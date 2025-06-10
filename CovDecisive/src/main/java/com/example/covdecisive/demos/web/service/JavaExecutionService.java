package com.example.covdecisive.demos.web.service;
import com.example.covdecisive.demos.web.model.SourceCode;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JavaExecutionService {

    public String executeJavaProject(List<SourceCode> SourceCodes) throws Exception {
        if (SourceCodes.isEmpty()) {
            throw new IllegalArgumentException("没有提供Java文件");
        }

        // 创建临时目录
        String tempDirName = "java_project_" + UUID.randomUUID();
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), tempDirName);
        Files.createDirectories(tempDir);

        try {
            // 写入文件到临时目录
            List<String> filePaths = new ArrayList<>();
            for (SourceCode sourceCode : SourceCodes) {
                Path filePath = tempDir.resolve(sourceCode.getFilePath());
                Files.write(filePath, sourceCode.getCodeContent().getBytes());
                filePaths.add(filePath.toString());
            }

            // 编译Java文件
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromStrings(filePaths);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    null,
                    null,
                    compilationUnits
            );

            boolean success = task.call();
            fileManager.close();

            if (!success) {
                StringBuilder error = new StringBuilder("编译错误:\n");
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    error.append(diagnostic.getMessage(null)).append("\n");
                }
                throw new RuntimeException(error.toString());
            }

            // 查找主类
            String mainClassName = findMainClass(SourceCodes);
            if (mainClassName == null) {
                throw new RuntimeException("找不到包含main方法的类");
            }

            // 执行主类
            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "-cp",
                    tempDir.toString(),
                    mainClassName
            );

            Process process = pb.start();

            // 获取输出
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("执行失败，退出码: " + exitCode + "\n错误: " + error);
            }

            return output;

        } finally {
            // 清理临时文件
            deleteDirectory(tempDir.toFile());
        }
    }

    private String findMainClass(List<SourceCode> files) {
        for (SourceCode file : files) {
            String content = file.getCodeContent();
            String fileName = file.getFilePath();

            if (fileName.endsWith(".java") &&
                    content.contains("public static void main") &&
                    content.contains("String[] args")) {

                // 提取类名
                String className = fileName.substring(0, fileName.length() - 5);
                return className;
            }
        }
        return null;
    }

    private String readStream(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}