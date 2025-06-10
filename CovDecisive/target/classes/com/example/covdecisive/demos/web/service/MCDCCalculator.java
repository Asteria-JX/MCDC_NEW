package com.example.covdecisive.demos.web.service;

import java.util.*;

/**
 * 与上面 MCDCCalculator.java 完全一致的代码模板，这里要放到资源里，
 * 以便 MCDCService 在运行时拷贝一份到临时目录，用于被测代码一起编译。
 */
public class MCDCCalculator {

    public static MCDCDetail computeMcdcDetail(List<MCDCHelper.DecisionTrace> traces) {
        if (traces.isEmpty()) {
            return new MCDCDetail(0, 0);
        }
        Map<String, Boolean> traceMap = new HashMap<>(traces.size());
        int nConds = traces.get(0).getCondValues().length;
        for (MCDCHelper.DecisionTrace dt : traces) {
            String key = arrayToKey(dt.getCondValues());
            traceMap.put(key, dt.getDecisionValue());
        }

        long covered = 0;
        long total = 0;

        for (Map.Entry<String, Boolean> entry : traceMap.entrySet()) {
            String key = entry.getKey();
            boolean decVal = entry.getValue();
            boolean[] conds = keyToArray(key);

            for (int i = 0; i < nConds; i++) {
                boolean[] flipped = conds.clone();
                flipped[i] = !flipped[i];
                String flippedKey = arrayToKey(flipped);
                total++;
                Boolean otherDec = traceMap.get(flippedKey);
                if (otherDec != null && otherDec != decVal) {
                    covered++;
                }
            }
        }

        covered /= 2;
        total /= 2;
        return new MCDCDetail(covered, total);
    }

    public static class MCDCDetail {
        public final long coveredCount;
        public final long totalCount;
        public final double ratio;

        public MCDCDetail(long coveredCount, long totalCount) {
            this.coveredCount = coveredCount;
            this.totalCount = totalCount;
            this.ratio = (totalCount == 0 ? 0.0 : (coveredCount * 1.0 / totalCount));
        }
    }

    private static String arrayToKey(boolean[] arr) {
        StringBuilder sb = new StringBuilder(arr.length);
        for (boolean b : arr) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }

    private static boolean[] keyToArray(String key) {
        boolean[] arr = new boolean[key.length()];
        for (int i = 0; i < key.length(); i++) {
            arr[i] = (key.charAt(i) == '1');
        }
        return arr;
    }
}
