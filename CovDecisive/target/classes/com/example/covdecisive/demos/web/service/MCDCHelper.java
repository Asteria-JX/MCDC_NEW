package com.example.covdecisive.demos.web.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * MCDCHelper：
 * 插桩时，在每个 if(...) 之前插入一行：
 * MCDCHelper.record(className, lineTag, condExprsArray, condValuesArray, decisionValue);
 * 运行测试时，各线程调用 record(...) 将数据存入 allTraces。
 * 测试结束后，MCDCRunner 会调用 dumpTracesToFile() 将所有轨迹序列化到文件。
 * MCDCService 再从文件中读取轨迹。
 */
public class MCDCHelper {
    // Key: "full.class.Name#LINE_X"，Value: list of 同一个 if 条件下的多次测试轨迹
    private static final Map<String, List<DecisionTrace>> allTraces = new HashMap<>();

    /**
     * 在每个 if(...) 之前由插桩语句调用。
     *
     * @param className     完整类名（package + "." + 类名），比如 "org.apache.commons.lang3.math.Fraction"
     * @param lineTag       行号标签，如 "LINE_1"、"LINE_2" … 用来区分同一个类中不同 if 语句
     * @param condExprs     每个子条件的文字形式数组（只是为了调试时能知道具体每个子条件长什么样）
     * @param condValues    每个子条件在当前测试运行时的布尔值，顺序须与 condExprs 一一对应
     * @param decisionValue 整体 if(...) 的判定结果（true/false）
     */
    public static synchronized void record(
            String className,
            String lineTag,
            String[] condExprs,
            boolean[] condValues,
            boolean decisionValue) {
        // !!! DEBUG LOG: 每次 record 时打印，确认插桩是否被触发 !!!
//        System.out.println("[DEBUG][MCDCHelper.record] Recording for: " + className + "#" + lineTag +
//                ", decision: " + decisionValue + ", condValues: " + Arrays.toString(condValues));

        String key = className + "#" + lineTag;
        allTraces.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new DecisionTrace(condValues, decisionValue));
    }

    /**
     * 将收集到的 MCDC 轨迹数据序列化并写入文件。
     * 该方法由 MCDCRunner 在测试运行结束后调用。
     *
     * @param outputPath 文件输出路径
     * @throws IOException 如果写入文件失败
     */
    public static void dumpTracesToFile(Path outputPath) throws IOException {
        // !!! 新增的调试日志：确保方法被调用时一定能打印 !!!
        System.out.println("[DEBUG][MCDCHelper.dumpTracesToFile] 进入 dumpTracesToFile 方法。当前 allTraces 大小: " + allTraces.size() + "，输出文件路径: " + outputPath);

        if (allTraces.isEmpty()) {
            System.out.println("[DEBUG][MCDCHelper.dumpTracesToFile] allTraces 为空。跳过文件写入。");
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(outputPath))) {
            oos.writeObject(allTraces);
            System.out.println("[DEBUG][MCDCHelper.dumpTracesToFile] 轨迹数据已成功倾倒。");
        } catch (Exception e) {
            System.err.println("[ERROR][MCDCHelper.dumpTracesToFile] 倾倒轨迹数据时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常，让调用者知道发生了错误
        }
    }
    /**
     * 从文件中反序列化并加载 MCDC 轨迹数据。
     * 该方法由 MCDCService 在测试进程结束后调用，用于读取数据。
     *
     * @param inputPath 文件输入路径
     * @return 存储的 MCDC 轨迹数据
     * @throws IOException            如果读取文件失败
     * @throws ClassNotFoundException 如果反序列化过程中找不到相应的类
     */
    public static Map<String, List<DecisionTrace>> loadTracesFromFile(Path inputPath) throws IOException, ClassNotFoundException {
        // !!! DEBUG LOG: 加载数据时打印文件路径 !!!
        System.out.println("[DEBUG][MCDCHelper.loadTracesFromFile] Attempting to load traces from file: " + inputPath);

        if (!Files.exists(inputPath) || Files.size(inputPath) == 0) {
            System.out.println("[DEBUG][MCDCHelper.loadTracesFromFile] Trace file does not exist or is empty. Returning empty map.");
            return new HashMap<>(); // 文件不存在或为空，返回空Map
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(inputPath))) {
            @SuppressWarnings("unchecked")
            Map<String, List<DecisionTrace>> loadedTraces = (Map<String, List<DecisionTrace>>) ois.readObject();
            System.out.println("[DEBUG][MCDCHelper.loadTracesFromFile] Successfully loaded " + loadedTraces.size() + " entries.");
            return loadedTraces;
        } catch (Exception e) {
            System.err.println("[ERROR][MCDCHelper.loadTracesFromFile] Error loading traces: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常
        }
    }

    /** 清空所有已记录的轨迹，方便多次运行时重置。 */
    public static void clearAllTraces() {
        System.out.println("[DEBUG][MCDCHelper.clearAllTraces] Clearing " + allTraces.size() + " recorded traces."); // DEBUG LOG
        allTraces.clear();
    }

    /**
     * 内部类：保存单次测试中，一条 if(...) 上的每个子条件值与最终判定值。
     * 必须实现 Serializable 接口才能被序列化。
     */
    public static class DecisionTrace implements Serializable {
        private static final long serialVersionUID = 1L; // 推荐添加 serialVersionUID
        private final boolean[] condValues;
        private final boolean decisionValue;

        public DecisionTrace(boolean[] condValues, boolean decisionValue) {
            this.condValues = condValues;
            this.decisionValue = decisionValue;
        }

        public boolean[] getCondValues() {
            return condValues;
        }

        public boolean getDecisionValue() {
            return decisionValue;
        }

        @Override
        public String toString() {
            return "DecisionTrace{" +
                    "condValues=" + Arrays.toString(condValues) +
                    ", decisionValue=" + decisionValue +
                    '}';
        }
    }
}