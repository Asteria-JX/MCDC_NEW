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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoberturaService {
    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private SourceCodeMapper sourceCodeMapper;

    @Autowired
    private TestProgramMapper testProgramMapper;

    @Autowired
    private TestResourceMapper testResourceMapper;

    @Value("${jacoco.agent.path}") // 确保在 application.properties 中配置 JaCoCo Agent 路径
    private String jacocoAgentPath;

    @Value("${jacococli.jar.path}") // 确保在 application.properties 中配置 jacococli.jar 路径
    private String jacococliJarPath;

    public void generate(Integer programId, Integer testResourceId, Integer way) throws Exception {
        // 获取源代码
        List<SourceCode> sourceCodes = sourceCodeMapper.selectByProgramId(programId);
        if (sourceCodes.isEmpty()) {
            throw new Exception("未找到对应 programId 的源代码");
        }

        // 创建临时工作目录
        Path workDir = Files.createTempDirectory("cobertura_project_");
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
        List<String> javacCommand = buildJavacCommand(srcDir, classesDir);//构建javac命令

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

        //✅第二部分
        // 获取测试用例
        List<TestResource> testResources = testResourceMapper.getTestResourceByProgramID(testResourceId);
        if (testResources.isEmpty()) {
            throw new Exception("未找到对应 testResourceId 的测试用例");
        }
        // 创建临时测试目录，注意：这里 testDir 应该用于存放测试用例的源代码，然后和 srcDir 一起编译
        // 但根据你的原始逻辑，测试用例也被写入了 srcDir，这里保持一致
        // Path testSourceDir = workDir.resolve("test-src"); // 如果想分离源代码和测试用例源代码，可以创建这个目录
        // Files.createDirectories(testSourceDir);


        // 将数据库源码写入本地临时文件 (测试用例)
        for (TestResource testResource : testResources) {
            if (!testResource.getFilePath().endsWith(".java")) {
                continue; // 跳过非Java文件
            }
            //把这段代码对应的文件名拼接成一个路径
            Path filePath = srcDir.resolve(testResource.getFilePath()); // 注意：这里测试用例也写入了 srcDir
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, testResource.getCodeContent(), StandardCharsets.UTF_8);
        }

        // 编译测试源码
        Path testclassesDir = workDir.resolve("test-classes");//创建classes目录用于保存编译后的.class文件
        Files.createDirectories(testclassesDir);
        List<String> javacCommand2 = buildJavacCommand2(srcDir, classesDir, testclassesDir);//构建javac命令

        //运行javac命令编译.java文件
        pbb = new ProcessBuilder(javacCommand2)
                .directory(workDir.toFile())
                .redirectErrorStream(true); // 合并 stdout 和 stderr
        javacProcess = pbb.start();
        reader = new BufferedReader(new InputStreamReader(javacProcess.getInputStream()));
        System.out.println(">>> javac 输出开始：");
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // 输出编译错误信息
        }
        exitCode = javacProcess.waitFor();
        System.out.println(">>> javac 退出码：" + exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("javac 编译失败！");
        }

        //✅第三部分 - 生成覆盖率报告
        generateCoverageReport(workDir, classesDir, testclassesDir);

        // 清理临时目录（可选）
        // FileSystemUtils.deleteRecursively(workDir);
    }

    // 构造 javac 命令参数
    private List<String> buildJavacCommand(Path srcDir, Path classesDir) throws IOException {
        List<String> files = Files.walk(srcDir)
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toString)
                .collect(Collectors.toList());
        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add("-encoding");
        cmd.add("UTF-8"); // 指定编码
        cmd.add("-cp");
        cmd.add("D:/Evosuite-1.0.5.2x/commons-lang3-3.12.0.jar"); // 添加依赖路径，注意这里是硬编码，可以考虑动态配置
        cmd.add("-d");
        cmd.add(classesDir.toString()); // 输出到 classes 目录
        cmd.addAll(files);
        return cmd;
    }

    private List<String> buildJavacCommand2(Path srcDir, Path classesDir, Path testclassesDir) throws IOException {
        List<String> files = Files.walk(srcDir) // 这里会再次编译所有 .java 文件，包括之前的源代码和测试用例
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toString)
                .collect(Collectors.toList());
        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add("-encoding");
        cmd.add("UTF-8"); // 指定编码
        cmd.add("-cp");
        // 包含源代码的编译输出目录和所有必要的依赖
        cmd.add(classesDir.toAbsolutePath().toString() + File.pathSeparator +
                "D:/Evosuite-1.0.5.2x/commons-lang3-3.12.0.jar" + File.pathSeparator +
                "D:/junit-jupiter-api-4.13.2.jar" + File.pathSeparator +
                "D:/junit-jupiter-engine-5.9.3.jar" + File.pathSeparator +
                "D:/hamcrest-core-1.3.jar" + File.pathSeparator +
                "D:/evosuite-standalone-runtime-1.0.6.jar" + File.pathSeparator +
                "D:/evosuite-1.0.6.jar"); // 硬编码依赖路径
        cmd.add("-d");
        cmd.add(testclassesDir.toString()); // 输出到 test-classes 目录
        cmd.addAll(files);
        return cmd;
    }

    private void generateCoverageReport(Path workDir, Path classesDir, Path testclassesDir) throws Exception {
        Path jacocoExecFile = workDir.resolve("jacoco.exec");
        Path reportDir = workDir.resolve("jacoco-report");
        Files.createDirectories(reportDir);

        // 1. 运行测试并生成 .exec 文件
        List<String> javaCommand = new ArrayList<>();
        javaCommand.add("java");
        // 添加 JaCoCo Agent
        javaCommand.add("-javaagent:" + jacocoAgentPath + "=destfile=" + jacocoExecFile.toAbsolutePath());
        javaCommand.add("-cp");

        // 构建 classpath，包含编译后的源代码、编译后的测试代码以及所有必要的依赖
        String classpath = classesDir.toAbsolutePath().toString() +
                File.pathSeparator + testclassesDir.toAbsolutePath().toString() +
                File.pathSeparator + "D:/Evosuite-1.0.5.2x/commons-lang3-3.12.0.jar" +
                File.pathSeparator + "D:/junit-jupiter-api-4.13.2.jar" +
                File.pathSeparator + "D:/junit-jupiter-engine-5.9.3.jar" +
                File.pathSeparator + "D:/hamcrest-core-1.3.jar" +
                File.pathSeparator + "D:/evosuite-standalone-runtime-1.0.6.jar" +
                File.pathSeparator + "D:/evosuite-1.0.6.jar"; // 确保这里包含了所有必要的依赖

        javaCommand.add(classpath);

        // 查找所有测试类并添加到命令中
        List<String> testClasses = Files.walk(testclassesDir)
                .filter(p -> p.toString().endsWith(".class") && !p.getFileName().toString().contains("$")) // 过滤掉内部类
                .map(p -> {
                    String relativePath = testclassesDir.relativize(p).toString();
                    return relativePath.substring(0, relativePath.length() - ".class".length()).replace(File.separatorChar, '.');
                })
                .collect(Collectors.toList());

        if (testClasses.isEmpty()) {
            System.out.println("WARN: 未找到任何测试类可运行。");
        } else {
            // 使用 JUnit 4 的测试运行器 JUnitCore
            javaCommand.add("org.junit.runner.JUnitCore");
            javaCommand.addAll(testClasses);

            System.out.println(">>> 运行测试命令: " + String.join(" ", javaCommand));
            ProcessBuilder pbTest = new ProcessBuilder(javaCommand)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true);
            Process testProcess = pbTest.start();
            BufferedReader testReader = new BufferedReader(new InputStreamReader(testProcess.getInputStream()));
            String testLine;
            System.out.println(">>> 测试运行输出开始：");
            while ((testLine = testReader.readLine()) != null) {
                System.out.println(testLine);
            }
            int testExitCode = testProcess.waitFor();
            System.out.println(">>> 测试运行退出码：" + testExitCode);

            if (testExitCode != 0) {
                System.err.println("测试运行失败，可能导致覆盖率报告不准确！");
            }
        }


        // 2. 使用 JaCoCo CLI 生成报告
        List<String> jacocoCommand = new ArrayList<>();
        jacocoCommand.add("java");
        jacocoCommand.add("-jar");
        jacocoCommand.add(jacococliJarPath); // 使用从配置中获取的 jacococli.jar 路径
        jacocoCommand.add("report");
        jacocoCommand.add(jacocoExecFile.toAbsolutePath().toString()); // JaCoCo exec 文件
        jacocoCommand.add("--classfiles");
        jacocoCommand.add(classesDir.toAbsolutePath().toString()); // 编译后的类文件
        jacocoCommand.add("--sourcefiles");
        jacocoCommand.add(workDir.resolve("src").toAbsolutePath().toString()); // 源代码目录
        jacocoCommand.add("--html");
        jacocoCommand.add(reportDir.toAbsolutePath().toString()); // 报告输出目录

        System.out.println(">>> JaCoCo 报告生成命令: " + String.join(" ", jacocoCommand));
        ProcessBuilder pbReport = new ProcessBuilder(jacocoCommand)
                .directory(workDir.toFile())
                .redirectErrorStream(true);
        Process reportProcess = pbReport.start();
        BufferedReader reportReader = new BufferedReader(new InputStreamReader(reportProcess.getInputStream()));
        String reportLine;
        System.out.println(">>> JaCoCo 报告生成输出开始：");
        while ((reportLine = reportReader.readLine()) != null) {
            System.out.println(reportLine);
        }
        int reportExitCode = reportProcess.waitFor();
        System.out.println(">>> JaCoCo 报告生成退出码：" + reportExitCode);

        if (reportExitCode != 0) {
            throw new RuntimeException("JaCoCo 报告生成失败！");
        }

        System.out.println("✅ 覆盖率报告已生成到: " + reportDir.toAbsolutePath());
    }
}


