package com.example.covdecisive.demos.web.service;

import com.example.covdecisive.demos.web.mapper.ProgramMapper;
import com.example.covdecisive.demos.web.mapper.SourceCodeMapper;
import com.example.covdecisive.demos.web.mapper.TestProgramMapper;
import com.example.covdecisive.demos.web.mapper.TestResourceMapper;

import com.example.covdecisive.demos.web.model.Program;
import com.example.covdecisive.demos.web.model.SourceCode;
import com.example.covdecisive.demos.web.model.TestProgram;
import com.example.covdecisive.demos.web.model.TestResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GenerateTestByRDService {

    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private SourceCodeMapper sourceCodeMapper;

    @Autowired
    private TestProgramMapper testProgramMapper;

    @Autowired
    private TestResourceMapper testResourceMapper;

    @Value("${randoop.path}")
    private String randoopPath;

    @Value("${java.home}")
    private String javaHome;

    // 新增配置项：用于指定源代码编译时所需的通用库路径 (例如 Apache Commons Lang 的 JAR 文件)
    @Value("${common.libs.path:}") // 如果未配置，则默认为空字符串
    private String commonLibsPath;

    private static final String TEMP_DIR_PREFIX = "randoop_temp_";

    @Transactional
    public String generateTestCasesWithRandoop(int programId,int userId) throws IOException, InterruptedException {
        System.out.println("--- 开始为程序ID " + programId + " 生成测试用例 ---");

        // 1. 从数据库中检索 Program 和 Source Code
        Optional<Program> programOptional = programMapper.selectByProgramId(programId);
        if (programOptional.isEmpty()) {
            System.err.println("错误：未找到程序ID为 " + programId + " 的项目。");
            throw new IllegalArgumentException("未找到程序ID为 " + programId + " 的项目。");
        }
        Program program = programOptional.get();

        List<SourceCode> sourceCodes = sourceCodeMapper.selectByProgramId(programId);

        System.out.println("已获取程序信息。从数据库查询到源代码记录数: " + sourceCodes.size());
        // 关键点：检查获取到的列表是否包含 null 元素
        boolean containsNull = sourceCodes.stream().anyMatch(java.util.Objects::isNull);
        if (containsNull) {
            System.err.println("警告：初始源代码列表中检测到 null 元素。");
        }

        // 如果列表为空（或者只包含 null 元素），则直接报错
        if (sourceCodes.isEmpty() || sourceCodes.stream().allMatch(java.util.Objects::isNull)) {
            System.err.println("错误：程序ID为 " + programId + " 的项目未找到任何有效的源代码记录。");
            throw new IllegalStateException("程序ID为 " + programId + " 的项目未找到任何有效的源代码记录。");
        }

        System.out.println("已获取程序信息和源代码。");

        String sanitizedProgramName = (program.getProgramName() != null ? program.getProgramName() : "unknown_program").replaceAll("\\s+", "_");
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX + sanitizedProgramName);
        Path sourceDir = Files.createDirectory(tempDir.resolve("src"));
        Path compileDir = Files.createDirectory(tempDir.resolve("classes"));
        Path randoopOutputDir = Files.createDirectory(tempDir.resolve("randoop-output"));
        System.out.println("已创建临时工作目录：" + tempDir.toAbsolutePath());


        // 2. 将源代码写入临时文件
        int actualWrittenFilesCount = 0; // 统计实际写入文件的数量
        int index = 0;
        for (SourceCode sc : sourceCodes) {
            System.out.println("--- 处理第 " + (index + 1) + " 条记录 ---");
            System.out.println("对象 SourceCode sc 是否为null: " + (sc == null)); // 更明确的 null 检查日志

            if (sc == null) {
                System.err.println("警告：sourceCodes 列表中包含 null 元素，已跳过第 " + (index + 1) + " 条记录的写入。");
                index++;
                continue;
            }

            // 调试点：打印 SourceCode 对象的详细信息
            System.out.println("源代码对象 (非null) 详情 - CodeId: " + sc.getCodeId() + ", FilePath: '" + sc.getFilePath() + "', ProgramId: " + sc.getProgramId());

            if (sc.getFilePath() == null || sc.getFilePath().trim().isEmpty()) { // 同时检查空字符串
                System.err.println("警告：源代码对象 (CodeId: " + sc.getCodeId() + ") 的 filePath 为 null 或空，已跳过写入。");
                index++;
                continue;
            }
            // 过滤非 Java 源文件，例如 package.html
            if (!sc.getFilePath().endsWith(".java") && !sc.getFilePath().endsWith(".jar")) { // 也允许写入jar文件，尽管通常不会是source_code表的内容
                System.out.println("跳过非Java源代码文件 (或非JAR文件): " + sc.getFilePath());
                index++;
                continue;
            }
            if (sc.getCodeContent() == null || sc.getCodeContent().trim().isEmpty()) { // 同时检查空字符串
                System.err.println("警告：源代码对象 (CodeId: " + sc.getCodeId() + ") 的 codeContent 为 null 或空，已跳过写入。");
                index++;
                continue;
            }

            Path filePath = sourceDir.resolve(sc.getFilePath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, sc.getCodeContent());
            System.out.println("已写入源代码文件: " + filePath.toAbsolutePath()); // 打印绝对路径
            actualWrittenFilesCount++;
            index++;
        }
        System.out.println("所有源代码已写入临时文件。实际写入文件数量: " + actualWrittenFilesCount);


        // 3. 编译源代码
        List<String> javaFiles = Files.walk(sourceDir)
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.toList());

        System.out.println("准备编译的 Java 文件数量: " + javaFiles.size()); // 新增日志
        System.out.println("准备编译的 Java 文件列表: " + javaFiles); // 新增日志

        if (javaFiles.isEmpty()) {
            System.err.println("错误：未找到可编译的 Java 文件。");
            throw new IllegalStateException("未找到可编译的 Java 文件。");
        }

        ProcessBuilder compileProcessBuilder = new ProcessBuilder();
        List<String> compileCommand = new java.util.ArrayList<>();
        compileCommand.add(javaHome + File.separator + "bin" + File.separator + "javac");

        // 新增：添加编译时所需的依赖库到 classpath
        // 注意：commonLibsPath 应配置为包含所有必要 JAR 文件的路径，可以是单个 JAR 文件路径或多个 JAR 文件路径用系统分隔符 (File.pathSeparator) 连接
        if (commonLibsPath != null && !commonLibsPath.isEmpty()) {
            compileCommand.add("-classpath");
            compileCommand.add(commonLibsPath);
        } else {
            System.err.println("警告：common.libs.path 未配置或为空，编译可能因缺少依赖而失败。请在 application.properties 或 application.yml 中配置 common.libs.path。");
        }

        compileCommand.add("-d");
        compileCommand.add(compileDir.toAbsolutePath().toString());
        compileCommand.addAll(javaFiles);

        System.out.println("编译命令: " + String.join(" ", compileCommand));
        compileProcessBuilder.command(compileCommand);
        compileProcessBuilder.redirectErrorStream(true);

        Process compileProcess = compileProcessBuilder.start();
        BufferedReader compileReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
        String line;
        StringBuilder compileOutput = new StringBuilder();
        while ((line = compileReader.readLine()) != null) {
            compileOutput.append(line).append("\n");
        }
        int compileExitCode = compileProcess.waitFor();
        System.out.println("编译输出:\n" + compileOutput.toString());
        if (compileExitCode != 0) {
            System.err.println("程序ID " + programId + " 的源代码编译失败。");
            throw new RuntimeException("源代码编译失败: " + compileOutput.toString());
        }
        System.out.println("源代码编译成功。");


        // 4. 确定 Randoop 要测试的类
        List<String> classNamesToTest = Files.walk(compileDir)
                .filter(p -> p.toString().endsWith(".class"))
                .map(p -> compileDir.relativize(p))
                .map(p -> p.toString().replace(File.separator, ".").replace(".class", ""))
                .collect(Collectors.toList());

        if (classNamesToTest.isEmpty()) {
            System.err.println("错误：未找到可供 Randoop 测试的编译后的类文件。");
            throw new IllegalStateException("未找到可供 Randoop 测试的编译后的类文件。");
        }
        System.out.println("将要测试的类：" + classNamesToTest);


        // 5. 执行 Randoop (以下代码保持不变)
        ProcessBuilder randoopProcessBuilder = new ProcessBuilder();
        List<String> randoopCommand = new java.util.ArrayList<>();
        randoopCommand.add(javaHome + File.separator + "bin" + File.separator + "java");
        randoopCommand.add("-classpath");
        // Randoop 的 classpath 需要包含编译后的类文件、Randoop JAR 以及被测试程序所需的任何运行时依赖
        String randoopClassPath = compileDir.toAbsolutePath().toString() +
                File.pathSeparator + randoopPath;
        if (commonLibsPath != null && !commonLibsPath.isEmpty()) {
            randoopClassPath += File.pathSeparator + commonLibsPath;
        }
        randoopCommand.add(randoopClassPath);
        randoopCommand.add("randoop.main.Main");
        randoopCommand.add("gentests");
        for (String className : classNamesToTest) {
            randoopCommand.add("--testclass=" + className);
        }
        randoopCommand.add("--junit-output-dir=" + randoopOutputDir.toString());
        randoopCommand.add("--log=RandoopLog.txt");

        System.out.println("Randoop 命令: " + String.join(" ", randoopCommand));
        randoopProcessBuilder.command(randoopCommand);
        randoopProcessBuilder.directory(tempDir.toFile());
        randoopProcessBuilder.redirectErrorStream(true);

        Process randoopProcess = randoopProcessBuilder.start();
        BufferedReader randoopReader = new BufferedReader(new InputStreamReader(randoopProcess.getInputStream()));
        StringBuilder randoopOutput = new StringBuilder();
        while ((line = randoopReader.readLine()) != null) {
            randoopOutput.append(line).append("\n");
        }
        int randoopExitCode = randoopProcess.waitFor();
        System.out.println("Randoop 输出:\n" + randoopOutput.toString());
        if (randoopExitCode != 0) {
            System.err.println("程序ID " + programId + " 的 Randoop 执行失败。");
            throw new RuntimeException("Randoop 执行失败: " + randoopOutput.toString());
        }
        System.out.println("Randoop 执行成功。");

        // 6. 将生成的测试用例保存到数据库
        List<Path> generatedTestFiles = Files.walk(randoopOutputDir)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());

        if (generatedTestFiles.isEmpty()) {
            System.out.println("警告：Randoop 未为程序ID " + programId + " 生成任何测试文件。这可能表示被测试的类或 Randoop 配置存在问题。");
        }

        TestProgram testProgram = new TestProgram();
//        testProgram.setProgramId(program.getProgramId());
        testProgram.setProgramId(programId);
        String testProgramBaseName = programMapper.selectProgramNameById(programId);
//        String testProgramBaseName = (program.getProgramName() != null ? program.getProgramName() : "UnknownProgram").replace(" ", "_");
        testProgram.setTestProgramName("Randoop_Generated_Tests_for_" + testProgramBaseName + "_" + UUID.randomUUID().toString().substring(0, 8));
        testProgram.setUserId(userId);
        testProgram.setCreateWay(2);
        testProgramMapper.insertTestProgram(testProgram);
        System.out.println("已创建新的测试项目：TestProgramId=" + testProgram.getTestProgramId());

        for (Path testFile : generatedTestFiles) {
            TestResource testResource = new TestResource();
            testResource.setTestProgramId(testProgram.getTestProgramId());
            testResource.setUserId(userId);
            testResource.setFilePath(randoopOutputDir.relativize(testFile).toString());;
            testResource.setCodeContent(Files.readString(testFile));
            testResourceMapper.insertTestResource(testResource);
            System.out.println("已保存测试资源文件: " + testFile.getFileName());
        }
        System.out.println("所有生成的测试用例文件已保存到数据库。");


        // 7. 清理临时目录（可选但强烈推荐）
        deleteDirectory(tempDir.toFile());
        System.out.println("已清理临时目录: " + tempDir);

        return "测试用例已成功生成并保存，程序 ID: " + programId + "。共生成 " + generatedTestFiles.size() + " 个测试文件。";
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] allContents = directory.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}