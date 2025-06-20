package com.example.covdecisive.demos.web.service;//package com.example.covdecisive.demos.web.service;
//
//import org.apache.commons.io.FileUtils;
//import org.jacoco.core.analysis.*;
//import org.jacoco.core.tools.*;
//import org.jacoco.report.*;
//import org.jacoco.report.html.HTMLFormatter;
//import org.junit.runner.*;
//import org.junit.runner.notification.RunListener;
//import org.springframework.stereotype.Service;
//
//import javax.tools.*;
//import java.io.*;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.nio.file.*;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Service
//public class CoverageService {
//
//    private static final String WORK_DIR = "coverage-temp";
//    private static final String REPORT_DIR = "coverage-reports";
//    private static final String AGENT_JAR_PATH = "D:/org.jacoco.agent-0.8.8.jar"; // 直接指定路径
//
//    public String runCoverageAnalysis(String mainClassCode, String testClassCode) throws Exception {
//        mainClassCode="public class Calculator {\n" +
//                "    public int add(int a, int b) {\n" +
//                "        return a + b;\n" +
//                "    }\n" +
//                "    \n" +
//                "    public int subtract(int a, int b) {\n" +
//                "        return a - b;\n" +
//                "    }\n" +
//                "    \n" +
//                "    public int multiply(int a, int b) {\n" +
//                "        return a * b;\n" +
//                "    }\n" +
//                "    \n" +
//                "    public double divide(int a, int b) {\n" +
//                "        if (b == 0) {\n" +
//                "            throw new IllegalArgumentException(\"Divisor cannot be zero\");\n" +
//                "        }\n" +
//                "        return (double) a / b;\n" +
//                "    }\n" +
//                "    \n" +
//                "    public boolean isEven(int number) {\n" +
//                "        return number % 2 == 0;\n" +
//                "    }\n" +
//                "}";
//            testClassCode="import org.junit.Test;\n" +
//                    "import static org.junit.Assert.*;\n" +
//                    "\n" +
//                    "public class CalculatorTest {\n" +
//                    "    private Calculator calculator = new Calculator();\n" +
//                    "    \n" +
//                    "    @Test\n" +
//                    "    public void testAdd() {\n" +
//                    "        assertEquals(5, calculator.add(2, 3));\n" +
//                    "    }\n" +
//                    "    \n" +
//                    "    @Test\n" +
//                    "    public void testSubtract() {\n" +
//                    "        assertEquals(1, calculator.subtract(3, 2));\n" +
//                    "    }\n" +
//                    "    \n" +
//                    "    @Test\n" +
//                    "    public void testMultiply() {\n" +
//                    "        assertEquals(6, calculator.multiply(2, 3));\n" +
//                    "    }\n" +
//                    "    \n" +
//                    "    @Test\n" +
//                    "    public void testDivide() {\n" +
//                    "        assertEquals(2.0, calculator.divide(4, 2), 0.001);\n" +
//                    "    }\n" +
//                    "    \n" +
//                    "    @Test(expected = IllegalArgumentException.class)\n" +
//                    "    public void testDivideByZero() {\n" +
//                    "        calculator.divide(4, 0);\n" +
//                    "    }\n" +
//                    "    \n" +
//                    "    @Test\n" +
//                    "    public void testIsEven() {\n" +
//                    "        assertTrue(calculator.isEven(4));\n" +
//                    "        assertFalse(calculator.isEven(5));\n" +
//                    "    }\n" +
//                    "}";
//        // 1. 准备临时目录
//        prepareDirectories();
//
//        // 2. 保存代码到文件
//        String mainClassName = extractClassName(mainClassCode);
//        String testClassName = extractClassName(testClassCode);
//
//        saveCodeToFile(mainClassName + ".java", mainClassCode);
//        saveCodeToFile(testClassName + ".java", testClassCode);
//
//        // 3. 编译代码
//        compileJavaFiles(mainClassName + ".java", testClassName + ".java");
//        System.out.println("编译成功");
//
//        // 4. 执行测试并收集覆盖率
//        File execDataFile = new File(WORK_DIR, "jacoco.exec");
//        runJUnitTests(testClassName, execDataFile);
//        System.out.println("测试成功");
//
//        // 5. 生成报告
//        return generateReport(execDataFile, mainClassName);
//    }
//
//    private void prepareDirectories() throws IOException {
//        File workDir = new File(WORK_DIR);
//        FileUtils.deleteDirectory(workDir);
//        workDir.mkdir();
//
//        File reportDir = new File(REPORT_DIR);
//        if (!reportDir.exists()) {
//            reportDir.mkdir();
//        }
//    }
//
//    private String extractClassName(String code) {
//        int classIndex = code.indexOf("class ") + 6;
//        int braceIndex = code.indexOf("{", classIndex);
//        return code.substring(classIndex, braceIndex).trim();
//    }
//
//    private void saveCodeToFile(String filename, String content) throws IOException {
//        Path filePath = Paths.get(WORK_DIR, filename);
//        Files.write(filePath, content.getBytes());
//    }
//
//    private void compileJavaFiles(String... filenames) throws IOException {
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
//
//        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
//            List<File> files = new ArrayList<>();
//            for (String filename : filenames) {
//                files.add(new File(WORK_DIR, filename));
//            }
//
//            Iterable<? extends JavaFileObject> compilationUnits =
//                    fileManager.getJavaFileObjectsFromFiles(files);
//
//            List<String> options = Arrays.asList("-d", WORK_DIR);
//
//            JavaCompiler.CompilationTask task = compiler.getTask(
//                    null, fileManager, diagnostics, options, null, compilationUnits);
//
//            if (!task.call()) {
//                StringBuilder errors = new StringBuilder("Compilation failed:\n");
//                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
//                    errors.append(diagnostic.getMessage(Locale.getDefault())).append("\n");
//                }
//                throw new RuntimeException(errors.toString());
//            }
//        }
//    }
//
////    private void runJUnitTests(String testClassName, File execDataFile) throws Exception {
////        // 直接使用硬编码路径
////        String agentJarPath = getAgentJarPath();
////
////        // 设置JVM参数
////        String jacocoOptions = String.format("-javaagent:%s=destfile=%s",
////                agentJarPath, execDataFile.getAbsolutePath());
////
////        // 使用特殊ClassLoader运行测试
////        URLClassLoader classLoader = new URLClassLoader(
////                new URL[]{new File(WORK_DIR).toURI().toURL()},
////                ClassLoader.getSystemClassLoader());
////
////        Class<?> testClass = classLoader.loadClass(testClassName);
////        JUnitCore junit = new JUnitCore();
////        junit.run(testClass);
////        // === 新增：验证jacoco.exec文件是否生成 ===
////        if (!execDataFile.exists()) {
////            throw new FileNotFoundException(
////                    "jacoco.exec文件未生成！\n" +
////                            "路径: " + execDataFile.getAbsolutePath() + "\n" +
////                            "工作目录内容: " + Arrays.toString(new File(WORK_DIR).listFiles())
////            );
////        }
////        System.out.println("jacoco.exec文件已生成: " + execDataFile.getAbsolutePath());
////    }
//    private void runJUnitTests(String testClassName, File execDataFile) throws Exception {
//        // 验证agent文件存在
//        String agentJarPath = getAgentJarPath();
//        File agentJar = new File(agentJarPath);
//        if (!agentJar.exists()) {
//            throw new FileNotFoundException("JaCoCo agent jar不存在: " + agentJarPath);
//        }
//
//        // 构建类路径（包括工作目录和项目依赖）
//        StringBuilder classpath = new StringBuilder();
//        classpath.append(new File(WORK_DIR).getAbsolutePath()).append(File.pathSeparator);
//
//        // 添加项目依赖（需根据实际情况调整）
//        classpath.append(System.getProperty("java.class.path"));
//
//        // 创建新JVM进程的命令
//        List<String> command = new ArrayList<>();
//        command.add(System.getProperty("java.home") + "/bin/java");
//        command.add("-javaagent:" + agentJarPath + "=destfile=" + execDataFile.getAbsolutePath());
//        command.add("-cp");
//        command.add(classpath.toString());
//        command.add("org.junit.runner.JUnitCore");
//        command.add(testClassName);
//
//        // 执行命令并获取输出
//        ProcessBuilder pb = new ProcessBuilder(command);
//        pb.directory(new File(WORK_DIR));
//        pb.redirectErrorStream(true); // 合并标准输出和错误输出
//
//        System.out.println("执行测试命令: " + String.join(" ", command));
//        Process process = pb.start();
//
//        // 读取进程输出
//        try (BufferedReader reader = new BufferedReader(
//                new InputStreamReader(process.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println("测试输出: " + line);
//            }
//        }
//
//        // 等待进程完成
//        int exitCode = process.waitFor();
//        if (exitCode != 0) {
//            throw new RuntimeException("测试进程退出码非0: " + exitCode);
//        }
//
//        // 验证exec文件生成
//        if (!execDataFile.exists()) {
//            throw new FileNotFoundException(
//                    "jacoco.exec文件未生成！\n" +
//                            "路径: " + execDataFile.getAbsolutePath() + "\n" +
//                            "工作目录内容: " + Arrays.toString(new File(WORK_DIR).listFiles())
//            );
//        }
//        System.out.println("jacoco.exec文件已生成: " + execDataFile.getAbsolutePath());
//    }
//
//    private String getAgentJarPath() throws Exception {
//        File agentJar = new File(AGENT_JAR_PATH);
//        if (!agentJar.exists()) {
//            throw new IllegalStateException("JaCoCo agent not found at: " + agentJar.getAbsolutePath());
//        }
//        return agentJar.getAbsolutePath();
//    }
//
//    private String generateReport(File execDataFile, String mainClassName) throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
//        File reportDir = new File(REPORT_DIR, "report-" + mainClassName + "-" + timestamp);
//        reportDir.mkdir();
//
//        // 加载执行数据
//        ExecFileLoader execFileLoader = new ExecFileLoader();
//        execFileLoader.load(execDataFile);
//
//        // 分析覆盖率
//        CoverageBuilder coverageBuilder = new CoverageBuilder();
//        Analyzer analyzer = new Analyzer(
//                execFileLoader.getExecutionDataStore(),
//                coverageBuilder);
//        analyzer.analyzeAll(new File(WORK_DIR));
//
//        // 生成HTML报告
//        HTMLFormatter htmlFormatter = new HTMLFormatter();
//        IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDir));
//
//        visitor.visitInfo(
//                execFileLoader.getSessionInfoStore().getInfos(),
//                execFileLoader.getExecutionDataStore().getContents());
//
//        visitor.visitBundle(
//                coverageBuilder.getBundle(mainClassName),
//                new DirectorySourceFileLocator(new File(WORK_DIR), "utf-8", 4));
//
//        visitor.visitEnd();
//
//        return reportDir.getAbsolutePath() + File.separator + "index.html";
//    }
//}

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CoverageService {

    // 假设项目结构为标准的Maven项目结构
    private static final String BASE_DIR = "jacoco_temp";
    private static final String MAIN_JAVA_DIR = BASE_DIR + "/src/main/java";
    private static final String TEST_JAVA_DIR = BASE_DIR + "/src/test/java";

    public String generateReport(String testClassName, String testClassCode,
                                 String targetClassName, String targetClassCode) {
        try {
            // 1. 准备目录结构
            prepareDirectoryStructure();

            // 2. 写入测试类和被测试类
            writeClassFile(MAIN_JAVA_DIR, targetClassName, targetClassCode);
            writeClassFile(TEST_JAVA_DIR, testClassName, testClassCode);

            // 3. 执行Maven命令生成报告
            executeMavenCommands();

            // 4. 返回报告路径
            return "JaCoCo report generated at: " +
                    Paths.get(BASE_DIR).toAbsolutePath() +
                    "/target/site/jacoco/index.html";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating JaCoCo report: " + e.getMessage();
        }
    }

    private void prepareDirectoryStructure() throws IOException {
        // 清理并创建目录
        Files.createDirectories(Paths.get(MAIN_JAVA_DIR));
        Files.createDirectories(Paths.get(TEST_JAVA_DIR));
    }

    private void writeClassFile(String baseDir, String className, String content) throws IOException {
        String packagePath = "";
        if (className.contains(".")) {
            packagePath = className.substring(0, className.lastIndexOf('.')).replace('.', '/');
            className = className.substring(className.lastIndexOf('.') + 1);
        }

        Path dirPath = Paths.get(baseDir, packagePath);
        Files.createDirectories(dirPath);

        Path filePath = dirPath.resolve(className + ".java");
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(content);
        }
    }

    private void executeMavenCommands() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(BASE_DIR));

        // 在Windows上
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("cmd.exe", "/c", "mvn clean test jacoco:report");
        }
        // 在Linux/Mac上
        else {
            processBuilder.command("sh", "-c", "mvn clean test jacoco:report");
        }

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Maven command execution failed with exit code: " + exitCode);
        }
    }
}