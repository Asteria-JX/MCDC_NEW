// MCDCRunner.java 的内容 (作为字符串在 MCDCService 中管理)
package com.example.covdecisive.demos.web.service;

import org.junit.runner.JUnitCore;
import java.nio.file.Paths;

/**
 * MCDCRunner: 一个包装器类，用于在独立的 JVM 进程中运行 JUnit 测试，
 * 并在测试结束后将 MCDCHelper 收集到的数据倾倒到文件。
 * 该类将由 MCDCService 动态生成并编译运行。
 */
public class MCDCRunner {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java MCDCRunner <output_file_path> <test_class_name>");
            System.exit(1);
        }
        String outputPathStr = args[0];
        String testClassName = args[1];

        System.out.println("[DEBUG][MCDCRunner] Starting JUnit tests for: " + testClassName);
        System.out.println("[DEBUG][MCDCRunner] Outputting traces to: " + outputPathStr);

        try {
            // 运行 JUnit 测试
            JUnitCore.main(testClassName);
            System.out.println("[DEBUG][MCDCRunner] JUnit tests finished.");

            // 在 JUnit 测试完成后，将收集到的 MCDC 轨迹数据写入文件
            MCDCHelper.dumpTracesToFile(Paths.get(outputPathStr));
            System.out.println("[DEBUG][MCDCRunner] MCDC traces dumped successfully by runner.");

        } catch (Exception e) {
            System.err.println("[DEBUG][MCDCRunner] Error during JUnit execution or trace dumping: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // 确保即使出错也退出
        } finally {
            // 确保在进程退出前清空数据，以便下次运行
            MCDCHelper.clearAllTraces();
        }
        System.exit(0); // 正常退出
    }
}