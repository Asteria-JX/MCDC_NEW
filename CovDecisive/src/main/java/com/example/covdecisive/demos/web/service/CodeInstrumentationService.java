package com.example.covdecisive.demos.web.service;

import com.example.covdecisive.demos.web.mapper.SourceCodeMapper;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator; // 通常用于离线插桩
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HexFormat; // Java 17+ 推荐使用，用于十六进制打印
import java.util.Arrays; // 如果 Java 版本较低，可以使用这个或自行实现十六进制转换

@Service
public class CodeInstrumentationService {
    @Autowired
    private SourceCodeMapper sourceCodeMapper;
    /**
     * 根据项目ID获取存储的字节码，对其进行JaCoCo插桩，并打印插桩后的字节码。
     *
     * @return 插桩后的字节码的十六进制字符串表示，如果找不到项目则返回 null
     * @throws RuntimeException 如果插桩过程中发生错误
     */
    public String instrumentAndPrintBytecode(String className,byte[] fileContent) throws IOException {
        Path classFilePath = Paths.get(
                "D:\\MCDC_back\\CovDecisive\\target\\classes\\com\\example\\covdecisive\\demos\\web\\service\\CalendarUnit.class"
        );

        System.out.println("----- 原始字节码 (" + className + ") Hex -----");
        // Java 17+
        System.out.println(HexFormat.of().formatHex(fileContent));
        // Java 8-16 可以用以下方式或自定义工具类
        // System.out.println(bytesToHex(originalBytecode));
        System.out.println("----------------------------------------");

        // 2. 使用 JaCoCo 进行插桩
        Instrumenter instrumenter = new Instrumenter(new OfflineInstrumentationAccessGenerator());
        byte[] instrumentedBytecode;
        try {
            instrumentedBytecode = instrumenter.instrument(fileContent, className);
        } catch (Exception e) {
            throw new RuntimeException("JaCoCo 插桩失败: " + e.getMessage(), e);
        }

        // 3. 将插桩后的字节码打印出来
        System.out.println("\n----- 插桩后的字节码 (" + className + ") Hex -----");
        // Java 17+
        System.out.println(HexFormat.of().formatHex(instrumentedBytecode));
        // Java 8-16
        // System.out.println(bytesToHex(instrumentedBytecode));
        System.out.println("----------------------------------------");

        // 获取源文件所在的目录
        Path parentDir = classFilePath.getParent();
        if (parentDir == null) {
            throw new IOException("无法获取原始类文件所在的目录：" + classFilePath);
        }

        // 构建插桩后文件的文件名，例如 "instrumented_CalendarUnit.class"
        String originalFileName = classFilePath.getFileName().toString(); // 获取 "CalendarUnit.class"
        String baseFileName = originalFileName.substring(0, originalFileName.lastIndexOf(".")); // 获取 "CalendarUnit"
        String instrumentedFileName = "instrumented_" + baseFileName + ".class";

        // 构建插桩后文件的完整路径：源目录 + 新文件名
        Path instrumentedFilePath = parentDir.resolve(instrumentedFileName);

//        try {
//            Files.write(instrumentedFilePath, instrumentedBytecode);
//            System.out.println("插桩后的字节码已保存到: " + instrumentedFilePath);
//        } catch (IOException e) {
//            throw new IOException("保存插桩后的字节码失败到 " + instrumentedFilePath + ": " + e.getMessage(), e);
//        }
        // 返回十六进制字符串，也可以直接返回 byte[]
//        return HexFormat.of().formatHex(instrumentedBytecode);
        return Base64.getEncoder().encodeToString(instrumentedBytecode);
    }

    // 如果你的 Java 版本低于 17 (例如 Java 8)，可以使用这个辅助方法将 byte[] 转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
