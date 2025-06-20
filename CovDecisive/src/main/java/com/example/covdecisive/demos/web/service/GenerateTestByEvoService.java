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
import org.springframework.util.FileSystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenerateTestByEvoService {
    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private SourceCodeMapper sourceCodeMapper;

    @Autowired
    private TestProgramMapper testProgramMapper;

    @Autowired
    private TestResourceMapper testResourceMapper;

    public void generate(Integer programId, Integer userId) throws Exception {
        // 获取源代码
        List<SourceCode> sourceCodes = sourceCodeMapper.selectByProgramId(programId);
        if (sourceCodes.isEmpty()) {
            throw new Exception("未找到对应 programId 的源代码");
        }

        // 创建临时工作目录
        Path workDir = Files.createTempDirectory("evosuite_project_");
        System.out.println("✅ 创建的临时工作目录为: " + workDir.toAbsolutePath());
        Path srcDir = workDir.resolve("src");
        Files.createDirectories(srcDir);

        // 将数据库源码写入本地临时文件
        for (SourceCode code : sourceCodes) {
            if (!code.getFilePath().endsWith(".java")) {
                continue; // 跳过非Java文件
            }
            //把这段代码对应的文件名拼接成一个路径
            Path filePath = srcDir.resolve(code.getFilePath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, code.getCodeContent(), StandardCharsets.UTF_8);
        }

        // 编译源码
        Path classesDir = workDir.resolve("classes");//创建classes目录用于保存编译后的.class文件
        Files.createDirectories(classesDir);
        List<String> javacCommand = buildJavacCommand(srcDir);//构建javac命令

        //运行javac命令编译.java文件
        ProcessBuilder pbb = new ProcessBuilder(javacCommand)
                .directory(workDir.toFile())
                .redirectErrorStream(true); // 合并 stdout 和 stderr
        Process javacProcess = pbb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(javacProcess.getInputStream()));
        String line;
        System.out.println(">>> javac 输出开始：");
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // 输出编译错误信息
        }
        int exitCode = javacProcess.waitFor();
        System.out.println(">>> javac 退出码：" + exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("javac 编译失败！");
        }


        // 执行 EvoSuite
        List<File> classFiles = Files.walk(classesDir)
                .filter(p -> p.toString().endsWith(".class"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        System.out.println(">>> 找到的 .class 文件如下：");
        for (File f : classFiles) {
            System.out.println(" - " + f.getAbsolutePath());
        }

        if (classFiles.isEmpty()) {
            System.out.println(">>> classes 目录内容为空或未生成 .class 文件");
            throw new Exception("未找到已编译类");
        }

        for (File classFile : classFiles) {
            String className = classesDir.relativize(classFile.toPath())
                    .toString()
                    .replace(File.separator, ".")
                    .replaceAll(".class$", "");

            System.out.println(">>> 调用 EvoSuite，类名: " + className);

            List<String> command = Arrays.asList(
                    "D:/JDK8/bin/java", "-jar", "D:/jars/evosuite-1.0.6.jar",
                    "-class", className,
                    "-projectCP", "D:/jars/commons-lang3-3.12.0.jar",
                    "-Dtest_dir=" + workDir.resolve("evosuite-tests").toString()
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader evoReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println(">>> EvoSuite 命令: " + String.join(" ", command));
            System.out.println(">>> EvoSuite 输出开始：");
            while ((line = evoReader.readLine()) != null) {
                System.out.println(line);
            }
            int evoExitCode = process.waitFor();
            System.out.println(">>> EvoSuite 退出码：" + evoExitCode);
        }

        // 保存测试类内容
        Path testDir = workDir.resolve("evosuite-tests");
        if (!Files.exists(testDir)) {
            throw new Exception("EvoSuite 测试目录未生成，可能执行失败");
        }


        // 检查是否已存在相同programId和userId的测试项目
        TestProgram existingTp = testProgramMapper.findByProgramIdAndUserId(programId, userId);
        TestProgram tp;
        //System.out.println("existingTp："+existingTp);

        if (existingTp != null) {
            // 使用已存在的测试项目
            tp = existingTp;
            System.out.println(">>> 使用已存在的测试项目，ID: " + tp.getTestProgramId());
        } else {
            // 创建新的测试项目
            tp = new TestProgram();
            tp.setProgramId(programId);
            tp.setCreateWay(1);
            tp.setUserId(userId);
            tp.setTestProgramName("Evosuite_program"+programId+"_user"+userId);
            testProgramMapper.insertTestProgram(tp);
            System.out.println(">>> 创建新的测试项目，ID: " + tp.getTestProgramId());
        }
        Files.walk(testDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        String code = Files.readString(path);
                        String filePath = testDir.relativize(path).toString()
                                .replace("evosuite-tests", "Evosuite")
                                .replace(File.separator, "/");
                        System.out.println(">>> 处理的 filePath: " + filePath); // 调试输出
                        // 检查是否已存在相同路径的资源
                        TestResource existingResource = testResourceMapper.findByTestProgramIdAndFilePath(tp.getTestProgramId(), filePath);
                        if (existingResource != null) {
                            // 更新现有资源
                            existingResource.setCodeContent(code);
                            testResourceMapper.updateCodeContent(existingResource);
                            System.out.println(">>> 更新现有测试资源，ID: " + existingResource.getId());
                        } else {
                            // 插入新资源
                            TestResource tr = new TestResource();
                            tr.setFilePath(filePath);
                            tr.setTestProgramId(tp.getTestProgramId());
                            tr.setUserId(userId);
                            tr.setCodeContent(code);
                            testResourceMapper.insertTestResource(tr);
                            System.out.println(">>> 插入新测试资源");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        // 清理临时目录（可选）
        FileSystemUtils.deleteRecursively(workDir);
    }

    // 构造 javac 命令参数
    private List<String> buildJavacCommand(Path srcDir) throws IOException {
        List<String> files = Files.walk(srcDir)
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toString)
                .collect(Collectors.toList());
        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add("-cp");
        cmd.add("D:/jars/commons-lang3-3.12.0.jar"); // 添加依赖路径
        cmd.add("-d");
        cmd.add(srcDir.getParent().resolve("classes").toString());
        cmd.addAll(files);
        return cmd;
    }

}
