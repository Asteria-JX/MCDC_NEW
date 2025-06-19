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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GeneratePythonTestService {

    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private SourceCodeMapper sourceCodeMapper;

    @Autowired
    private TestProgramMapper testProgramMapper;

    @Autowired
    private TestResourceMapper testResourceMapper;

    @Value("${python.executable}")
    private String pythonExecutable; // 从配置文件读取 Python 可执行文件路径

    private static final String TEMP_DIR_PREFIX = "pynguin_temp_";

    @Transactional
    public String generatePynguinTestsForProject(int programId, int userId) throws IOException, InterruptedException {
        System.out.println("--- 开始为 Python 程序ID " + programId + " 生成整个项目的测试用例 ---");

        // 1. 从数据库中检索 Program 和 Source Code
        Optional<Program> programOptional = programMapper.selectByProgramId(programId);
        if (programOptional.isEmpty()) {
            System.err.println("错误：未找到程序ID为 " + programId + " 的项目。");
            throw new IllegalArgumentException("未找到程序ID为 " + programId + " 的项目。");
        }
        Program program = programOptional.get();

        List<SourceCode> sourceCodes = sourceCodeMapper.selectByProgramId(programId);

        if (sourceCodes.isEmpty()) {
            System.err.println("错误：程序ID为 " + programId + " 的项目未找到任何源代码记录。");
            throw new IllegalStateException("程序ID为 " + programId + " 的项目未找到任何源代码记录。");
        }

        System.out.println("已获取 Python 程序信息和源代码。");

        String sanitizedProgramName = (program.getProgramName() != null ? program.getProgramName() : "unknown_python_program").replaceAll("\\s+", "_");
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX + sanitizedProgramName);
        Path pythonSourceRoot = tempDir.resolve("python_project_root"); // Pynguin 的 --project-path
        Path pynguinOutputDir = tempDir.resolve("pynguin_output"); // Pynguin 生成测试用例的输出目录
        Path pynguinReportDir = tempDir.resolve("pynguin_report"); // Pynguin 生成报告目录的输出目录
        Files.createDirectories(pythonSourceRoot);
        Files.createDirectories(pynguinOutputDir);
        Files.createDirectories(pynguinReportDir);
        System.out.println("已创建临时工作目录：" + tempDir.toAbsolutePath());

        // 2. 将 Python 源代码写入临时文件，并保持其原始目录结构
        int actualWrittenFilesCount = 0;
        List<String> pythonFilePaths = new ArrayList<>(); // 存储所有写入的 .py 文件的相对路径
        for (SourceCode sc : sourceCodes) {
            if (sc == null || sc.getFilePath() == null || sc.getFilePath().trim().isEmpty() || sc.getCodeContent() == null || sc.getCodeContent().trim().isEmpty()) {
                System.err.println("警告：跳过无效的源代码记录 (CodeId: " + (sc != null ? sc.getCodeId() : "null") + ")。");
                continue;
            }
            // 过滤非 .py 文件，以及排除以 .开头的隐藏文件或特殊文件（如.gitignore）
            if (!sc.getFilePath().endsWith(".py") || sc.getFilePath().startsWith(".")) {
                System.out.println("跳过非 Python 源代码文件或非有效源文件: " + sc.getFilePath());
                continue;
            }

            Path filePathInTemp = pythonSourceRoot.resolve(sc.getFilePath());
            Files.createDirectories(filePathInTemp.getParent()); // 确保父目录存在
            Files.writeString(filePathInTemp, sc.getCodeContent());
            System.out.println("已写入 Python 源代码文件: " + filePathInTemp.toAbsolutePath());
            actualWrittenFilesCount++;
            pythonFilePaths.add(sc.getFilePath()); // 收集相对路径
        }
        System.out.println("所有 Python 源代码已写入临时文件。实际写入文件数量: " + actualWrittenFilesCount);

        if (actualWrittenFilesCount == 0) {
            System.err.println("错误：未找到任何有效的 Python 源文件可供 Pynguin 测试。");
            throw new IllegalStateException("未找到任何有效的 Python 源文件可供 Pynguin 测试。");
        }

        // 3. 确定所有要测试的 Python 模块
        List<String> modulesToTest = new ArrayList<>();
        // 假设项目根目录就是 pythonSourceRoot
        for (String relativePath : pythonFilePaths) {
            // 排除 __init__.py 和测试文件本身 (如果数据库中包含测试文件)
            if (relativePath.contains("__init__.py") || relativePath.startsWith("test_")) {
                continue;
            }
            // 将文件路径转换为 Python 模块路径
            // 例如 "python_example/calculator/calculator.py" -> "python_example.calculator.calculator"
            String moduleName = relativePath
                    .replace("/", ".") // 替换正斜杠为点
                    .replace("\\", ".") // 替换反斜杠为点 (针对Windows路径)
                    .replace(".py", "");       // 移除 .py 扩展名
            modulesToTest.add(moduleName);
        }

        if (modulesToTest.isEmpty()) {
            System.err.println("警告：在项目 " + programId + " 中未找到可供 Pynguin 测试的 Python 模块。");
            return "没有找到可供 Pynguin 测试的 Python 模块。";
        }
        System.out.println("检测到以下 Python 模块将进行测试: " + modulesToTest);

        // 4. 逐个模块执行 Pynguin
        List<Path> allGeneratedTestFiles = new ArrayList<>();
        for (String module : modulesToTest) {
            System.out.println("\n--- 调用 Pynguin 为模块: " + module + " 生成测试用例 ---");

            List<String> pynguinCommand = new ArrayList<>();
            pynguinCommand.add(pythonExecutable);
            pynguinCommand.add("-m");
            pynguinCommand.add("pynguin");

            pynguinCommand.add("--project-path");
            pynguinCommand.add(pythonSourceRoot.toAbsolutePath().toString());

            pynguinCommand.add("--output-path");
            pynguinCommand.add(pynguinOutputDir.toAbsolutePath().toString());

            pynguinCommand.add("--report-dir");
            pynguinCommand.add(pynguinReportDir.toAbsolutePath().toString());


//            pynguinCommand.add("--module");
            pynguinCommand.add("--module-name");
            pynguinCommand.add(module);

            // 可以根据需要添加其他 Pynguin 参数
            // pynguinCommand.add("--type-inference-timeout");
            // pynguinCommand.add("60");

            System.out.println("Pynguin 命令: " + String.join(" ", pynguinCommand));

            ProcessBuilder pynguinProcessBuilder = new ProcessBuilder(pynguinCommand);
            // Pynguin 的工作目录设置在临时目录的根部，以便它能找到 --project-path 指定的 Python 文件
            pynguinProcessBuilder.directory(tempDir.toFile());
            pynguinProcessBuilder.redirectErrorStream(true);

            Process pynguinProcess = pynguinProcessBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(pynguinProcess.getInputStream()));
            StringBuilder pynguinOutput = new StringBuilder();
            String pynguinLine;
            while ((pynguinLine = reader.readLine()) != null) {
                pynguinOutput.append(pynguinLine).append("\n");
            }

            int pynguinExitCode = pynguinProcess.waitFor();

            System.out.println("Pynguin 输出 (模块 " + module + "):\n" + pynguinOutput.toString());

            if (pynguinExitCode != 0) {
                System.err.println("模块 " + module + " 的 Pynguin 执行失败。继续处理下一个模块...");
            } else {
                System.out.println("模块 " + module + " 的 Pynguin 执行成功。");
                // 收集当前模块生成的所有测试文件
                Files.walk(pynguinOutputDir)
                        .filter(p -> p.toString().endsWith(".py") && p.getFileName().toString().startsWith("test_"))
                        .forEach(allGeneratedTestFiles::add);
            }
        }

        if (allGeneratedTestFiles.isEmpty()) {
            System.out.println("警告：Pynguin 未为程序ID " + programId + " 生成任何测试文件。这可能表示被测试的模块或 Pynguin 配置存在问题。");
        } else {
            System.out.println("Pynguin 已成功生成总共 " + allGeneratedTestFiles.size() + " 个测试文件。");
            // 5. 将生成的测试用例保存到数据库
            TestProgram testProgram = new TestProgram();
            testProgram.setProgramId(programId);
//        String testProgramBaseName = program.getProgramName() != null ? program.getProgramName() : "UnknownProgram";
            String testProgramBaseName = programMapper.selectProgramNameById(programId);
            testProgram.setTestProgramName("Pynguin_Generated_Tests_for_" + testProgramBaseName + "_userId:" + userId + "_" + UUID.randomUUID().toString().substring(0, 8));
            testProgram.setUserId(userId);
            testProgram.setCreateWay(4); // 假设 4 代表 Pynguin 生成
            testProgramMapper.insertTestProgram(testProgram);
            System.out.println("已创建新的测试项目：TestProgramId=" + testProgram.getTestProgramId());

            for (Path testFile : allGeneratedTestFiles) {
                TestResource testResource = new TestResource();
                testResource.setTestProgramId(testProgram.getTestProgramId());
                testResource.setUserId(userId);
                // 保存相对路径，确保在读取时能正确重构
                testResource.setFilePath(pynguinOutputDir.relativize(testFile).toString());
                testResource.setCodeContent(Files.readString(testFile));
                testResourceMapper.insertTestResource(testResource);
                System.out.println("已保存测试资源文件: " + testFile.getFileName());
            }
            System.out.println("所有生成的测试用例文件已保存到数据库。");
        }

//        // 5. 将生成的测试用例保存到数据库
//        TestProgram testProgram = new TestProgram();
//        testProgram.setProgramId(programId);
////        String testProgramBaseName = program.getProgramName() != null ? program.getProgramName() : "UnknownProgram";
//        String testProgramBaseName = programMapper.selectProgramNameById(programId);
//        testProgram.setTestProgramName("Pynguin_Generated_Tests_for_" + testProgramBaseName + "_userId:" + userId);
//        testProgram.setUserId(userId);
//        testProgram.setCreateWay(3); // 假设 3 代表 Pynguin 生成
//        testProgramMapper.insertTestProgram(testProgram);
//        System.out.println("已创建新的测试项目：TestProgramId=" + testProgram.getTestProgramId());
//
//        for (Path testFile : allGeneratedTestFiles) {
//            TestResource testResource = new TestResource();
//            testResource.setTestProgramId(testProgram.getTestProgramId());
//            testResource.setUserId(userId);
//            // 保存相对路径，确保在读取时能正确重构
//            testResource.setFilePath(pynguinOutputDir.relativize(testFile).toString());
//            testResource.setCodeContent(Files.readString(testFile));
//            testResourceMapper.insertTestResource(testResource);
//            System.out.println("已保存测试资源文件: " + testFile.getFileName());
//        }
//        System.out.println("所有生成的测试用例文件已保存到数据库。");

        // 6. 获取report_dir中的获取 Pynguin 生成的 statistics.csv 内容
        Path statisticsFile = pynguinReportDir.resolve("statistics.csv");
        if (Files.exists(statisticsFile)) {
            try {
                // 读取 statistics.csv 内容
                String statisticsContent = Files.readString(statisticsFile);
                System.out.println("Pynguin 统计信息:\n" + statisticsContent);

                // 如果需要，可以将统计信息保存到数据库或其他地方
                // saveStatisticsToDatabase(statisticsContent, testProgram.getTestProgramId());
            } catch (IOException e) {
                System.err.println("无法读取统计文件: " + e.getMessage());
            }
        } else {
            System.out.println("警告: 未找到统计文件 statistics.csv");
        }

        // 7. 清理临时目录
        deleteDirectory(tempDir.toFile());
        System.out.println("已清理临时目录: " + tempDir);

        if (allGeneratedTestFiles.isEmpty()) {
            System.out.println("警告：Pynguin 未为程序ID " + programId + " 生成任何测试文件。这可能表示被测试的模块或 Pynguin 配置存在问题。");
            return "警告：Pynguin 未为程序ID " + programId + " 生成任何测试文件。这可能表示被测试的模块或 Pynguin 配置存在问题。";
        } else {
            return "测试用例已成功生成并保存，程序 ID: " + programId + "。共生成 " + allGeneratedTestFiles.size() + " 个测试文件。";
        }
    }

    // 辅助方法：删除目录及其内容
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