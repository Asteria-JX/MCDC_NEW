////package com.example.covdecisive.demos.web.service;
////
////import com.example.covdecisive.demos.web.model.SourceCode;
////import com.example.covdecisive.demos.web.model.TestResource;
////import org.aspectj.apache.bcel.classfile.Code;
////import org.junit.runner.JUnitCore;
////import org.junit.runner.Result;
////import org.junit.runner.notification.Failure;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import javax.tools.*;
////import java.io.File;
////import java.io.IOException;
////import java.lang.reflect.Method;
////import java.net.URI;
////import java.net.URL;
////import java.net.URLClassLoader;
////import java.nio.file.Files;
////import java.nio.file.Path;
////import java.util.Arrays;
////import java.util.List;
////
////@Service
////public class CodeService {
////    public String runTests(SourceCode sourceCode, TestResource testResource) throws Exception {
////        // 从数据库中读取 Calculator 和 CalculatorTest 的代码
////        String calculatorCode = sourceCode.getCodeContent();
//////        String calculatorTestCode = testResource.getCodeContent();
//////        String calculatorCode = "public class Calculator {\n" +
//////                "    public int add(int a, int b) {\n" +
//////                "        return a + b;\n" +
//////                "    }\n" +
//////                "}";
//////
////        String calculatorTestCode = "import org.junit.Test;\n" +  // 使用 JUnit 4 的注解
////                "import static org.junit.Assert.assertEquals;\n" + // 使用 JUnit 4 的断言
////                "\n" +
////                "public class CalculatorTest {\n" +
////                "    @Test\n" +
////                "    public void testAdd() {\n" +
////                "        Calculator calculator = new Calculator();\n" +
////                "        assertEquals(5, calculator.add(2, 3));\n" +
////                "    }\n" +
////                "\n" +
////                "    @Test\n" +
////                "    public void testSubtract() {\n" +
////                "        Calculator calculator = new Calculator();\n" +
////                "        assertEquals(1, calculator.add(2, 3));\n" +
////                "    }\n" +
////                "}";
////
////        if (calculatorCode == null || calculatorTestCode == null) {
////            return "Error: Calculator or CalculatorTest code not found in the database.";
////        }
////
////        // 创建临时目录
////        Path tempDir = Files.createTempDirectory("code-");
////        Path calculatorPath = tempDir.resolve(sourceCode.getFilePath());
////        Path calculatorTestPath = tempDir.resolve("CalculatorTest.java");
////
////
////        // 将代码写入文件
////        Files.write(calculatorPath, calculatorCode.getBytes());
////        Files.write(calculatorTestPath, calculatorTestCode.getBytes());
////
////        // 动态编译代码
////        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
////        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
////        Iterable<? extends JavaFileObject> compilationUnits = stdManager.getJavaFileObjectsFromStrings(
////                Arrays.asList(calculatorPath.toString(), calculatorTestPath.toString()));
////        JavaCompiler.CompilationTask task = compiler.getTask(null, stdManager, null, null, null, compilationUnits);
////        if (!task.call()) {
////            return "Compilation failed";
////        }
////
////        // 动态加载编译后的类
////        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toUri().toURL()});
////        Class<?> calculatorTestClass = Class.forName("CalculatorTest", true, classLoader);
////
////        // 运行测试
////        Result result = JUnitCore.runClasses(calculatorTestClass);
////        String re=formatTestResult(result);
////        System.out.println(re);
////        return re;
////    }
////
////    private String formatTestResult(Result result) {
////        StringBuilder sb = new StringBuilder();
////        sb.append("Tests run: ").append(result.getRunCount()).append(", ");
////        sb.append("Failures: ").append(result.getFailureCount()).append(", ");
////        sb.append("Errors: ").append(result.getIgnoreCount()).append("\n");
////        for (Failure failure : result.getFailures()) {
////            sb.append("Failure: ").append(failure.toString()).append("\n");
////        }
////        return sb.toString();
////    }
////}
//
//package com.example.covdecisive.demos.web.service;
//
//import com.example.covdecisive.demos.web.model.SourceCode;
//import com.example.covdecisive.demos.web.model.TestResource;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//import org.junit.runner.notification.Failure;
//import org.springframework.stereotype.Service;
//
//import javax.tools.*;
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.net.URI;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class CodeService {
//    public String runTests(List<SourceCode> sourceCodes, TestResource testResource) throws Exception {
//        // 从数据库中读取 Calculator 和 CalculatorTest 的代码
////        String calculatorTestCode = "import org.junit.Test;\n" +  // 使用 JUnit 4 的注解
////                "import static org.junit.Assert.assertEquals;\n" + // 使用 JUnit 4 的断言
////                "\n" +
////                "public class CalculatorTest {\n" +
////                "    @Test\n" +
////                "    public void testAdd() {\n" +
////                "        Calculator calculator = new Calculator();\n" +
////                "        assertEquals(5, calculator.add(2, 3));\n" +
////                "    }\n" +
////                "\n" +
////                "    @Test\n" +
////                "    public void testSubtract() {\n" +
////                "        Calculator calculator = new Calculator();\n" +
////                "        assertEquals(1, calculator.add(2, 3));\n" +
////                "    }\n" +
////                "}";
//
//        String calculatorTestCode =testResource.getCode_content();
//        if (sourceCodes == null || calculatorTestCode == null) {
//            return "Error: Source codes or CalculatorTest code not found in the database.";
//        }
//
//        // 创建临时目录
//        Path tempDir = Files.createTempDirectory("code-");
//        System.out.println("Temp directory created: " + tempDir);
//
//        // 将代码写入文件
//        List<Path> sourcePaths = new ArrayList<>();
//        for (SourceCode sourceCode : sourceCodes) {
//            Path sourcePath = tempDir.resolve(sourceCode.getFilePath());
//            Files.write(sourcePath, sourceCode.getCodeContent().getBytes());
//            sourcePaths.add(sourcePath);
//            System.out.println("Written file: " + sourcePath);
//        }
//
//        Path calculatorTestPath = tempDir.resolve(testResource.getName());
////        Path calculatorTestPath = tempDir.resolve(testResource.getName());
//        System.out.println(testResource.getName());
//        Files.write(calculatorTestPath, calculatorTestCode.getBytes());
//        sourcePaths.add(calculatorTestPath);
//        System.out.println("Written file: " + calculatorTestPath);
//
//        // 动态编译代码
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
//        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
//
//        // 打印要编译的文件列表
//        System.out.println("Compiling files:");
//        for (Path path : sourcePaths) {
//            System.out.println("  - " + path);
//        }
//
//        Iterable<? extends JavaFileObject> compilationUnits = stdManager.getJavaFileObjects(
//                sourcePaths.stream().map(Path::toFile).toArray(File[]::new));
//
//        JavaCompiler.CompilationTask task = compiler.getTask(null, stdManager, diagnostics, null, null, compilationUnits);
//
//        boolean success = task.call();
//
//        // 无论编译是否成功都打印诊断信息
//        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
//            System.out.printf("Diagnostic: %s%n", diagnostic.getMessage(null));
//        }
//
//        if (!success) {
//            StringBuilder sb = new StringBuilder("Compilation failed:\n");
//            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
//                sb.append(diagnostic.getMessage(null)).append("\n");
//            }
//            System.out.println(sb.toString());
//            return sb.toString();
//        } else {
//            System.out.println("Compilation succeeded.");
//        }
//
//        // 动态加载编译后的类
//        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toUri().toURL()});
//        try {
//            System.out.println("Attempting to load class: CalculatorTest");
//            Class<?> calculatorTestClass = Class.forName(testResource.getName().replace(".java", ""), true, classLoader);
//            System.out.println("Class loaded successfully: " + calculatorTestClass.getName());
//
//            // 运行测试
//            System.out.println("Running JUnit tests...");
//            Result result = JUnitCore.runClasses(calculatorTestClass);
//            String formattedResult = formatTestResult(result);
//            System.out.println("Test results:\n" + formattedResult);
//            return formattedResult;
//        } catch (ClassNotFoundException e) {
//            System.err.println("Class not found exception: " + e.getMessage());
//            e.printStackTrace();
//            return "Class not found: " + e.getMessage();
//        } catch (Exception e) {
//            System.err.println("Unexpected error: " + e.getMessage());
//            e.printStackTrace();
//            return "Error running tests: " + e.getMessage();
//        } finally {
//            classLoader.close();
//            // 可选：删除临时目录
//            // deleteDirectory(tempDir.toFile());
//        }
//    }
//
//    private String formatTestResult(Result result) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Tests run: ").append(result.getRunCount()).append(", ");
//        sb.append("Failures: ").append(result.getFailureCount()).append(", ");
//        sb.append("Ignored: ").append(result.getIgnoreCount()).append("\n");
//
//        if (result.getFailureCount() > 0) {
//            sb.append("Failures:\n");
//            for (Failure failure : result.getFailures()) {
//                sb.append("  - ").append(failure.getTestHeader()).append(": ");
//                sb.append(failure.getMessage()).append("\n");
//                // 打印堆栈跟踪（可选）
//                // sb.append(failure.getTrace()).append("\n");
//            }
//        }
//
//        return sb.toString();
//    }
//
//    // 递归删除目录的辅助方法
//    private void deleteDirectory(File directory) {
//        if (directory.exists()) {
//            File[] files = directory.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isDirectory()) {
//                        deleteDirectory(file);
//                    } else {
//                        file.delete();
//                    }
//                }
//            }
//            directory.delete();
//        }
//    }
//}