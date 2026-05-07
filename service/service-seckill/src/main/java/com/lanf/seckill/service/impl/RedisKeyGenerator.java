package com.lanf.seckill.service.impl;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RedisKeyGenerator {

    private static final int REDIS_CLUSTER_SLOTS = 16384;
    private static final int NODE_COUNT = 6;
    
    /**
     * 数字后缀的范围：1-8
     */
    public static final int DIGIT_SUFFIX_MIN = 1;
    public static final int DIGIT_SUFFIX_MAX = 8;
    
    /**
     * 所有数字后缀的集合 [1, 2, 3, 4, 5, 6, 7, 8]
     */
    public static final List<Integer> ALL_DIGIT_SUFFIXES = Collections.unmodifiableList(
            IntStream.rangeClosed(DIGIT_SUFFIX_MIN, DIGIT_SUFFIX_MAX)
                    .boxed()
                    .collect(Collectors.toList())
    );

    private static final Map<Integer, String> DIGIT_TO_SUFFIX_MAP = new HashMap<>();
    private static final Map<String, Integer> SUFFIX_TO_DIGIT_MAP = new HashMap<>();

    static {
        initializeSuffixMapping();
    }

    private static void initializeSuffixMapping() {
        String[] suffixes = findOptimalSuffixes();
        
        for (int i = 1; i <= DIGIT_SUFFIX_MAX; i++) {
            String suffix = suffixes[i - 1];
            DIGIT_TO_SUFFIX_MAP.put(i, suffix);
            SUFFIX_TO_DIGIT_MAP.put(suffix, i);
        }
    }

    private static String[] findOptimalSuffixes() {
        String[] result = new String[DIGIT_SUFFIX_MAX];
        int[] nodeCount = new int[NODE_COUNT];
        
        for (int digit = DIGIT_SUFFIX_MIN; digit <= DIGIT_SUFFIX_MAX; digit++) {
            int targetNode = findMinLoadNode(nodeCount);
            String suffix = findSuffixForNode(targetNode, digit);
            result[digit - 1] = suffix;
            nodeCount[targetNode]++;
        }
        
        return result;
    }

    private static int findMinLoadNode(int[] nodeCount) {
        int minNode = 0;
        int minCount = Integer.MAX_VALUE;
        for (int i = 0; i < NODE_COUNT; i++) {
            if (nodeCount[i] < minCount) {
                minCount = nodeCount[i];
                minNode = i;
            }
        }
        return minNode;
    }

    private static String findSuffixForNode(int targetNode, int digit) {
        for (int seq = 0; seq < 1000; seq++) {
            String suffix;
            if (seq == 0) {
                suffix = "d" + digit;
            } else {
                suffix = "d" + digit + "_" + seq;
            }
            
            int slot = calculateSlot("key:" + suffix);
            int node = slot / (REDIS_CLUSTER_SLOTS / NODE_COUNT);
            
            if (node == targetNode) {
                return suffix;
            }
        }
        
        return "d" + digit;
    }

    private static int calculateSlot(String key) {
        CRC16 crc16 = new CRC16();
        crc16.update(key.getBytes(StandardCharsets.UTF_8));
        return (int) (crc16.getValue() & 0x3FFF);
    }

    /**
     * 根据数字 1-8 生成 Redis key 后缀
     * 
     * @param digit 1-8 的数字
     * @return key 后缀，如 "_d1", "_d2_5" 等
     */
    public String generateKeySuffixByDigit(int digit) {
        if (digit < DIGIT_SUFFIX_MIN || digit > DIGIT_SUFFIX_MAX) {
            throw new IllegalArgumentException("Digit must be between " + DIGIT_SUFFIX_MIN + 
                    " and " + DIGIT_SUFFIX_MAX + ", got: " + digit);
        }
        String suffix = DIGIT_TO_SUFFIX_MAP.get(digit);
        if (suffix == null) {
            throw new IllegalStateException("No suffix mapping found for digit: " + digit);
        }
        return "_" + suffix;
    }

    /**
     * 根据完整的 Redis key 反推原始数字
     * 
     * @param fullKey 完整的 Redis key，如 "seckill:item:123_d1"
     * @return 原始数字 1-8，如果无法解析返回 -1
     */
    public int extractDigitFromKey(String fullKey) {
        if (fullKey == null || !fullKey.contains("_")) {
            return -1;
        }
        
        int lastUnderscore = fullKey.lastIndexOf('_');
        String suffix = fullKey.substring(lastUnderscore + 1);
        
        Integer digit = SUFFIX_TO_DIGIT_MAP.get(suffix);
        if (digit != null) {
            return digit;
        }
        
        if (suffix.startsWith("d") && suffix.length() >= 2) {
            try {
                char firstChar = suffix.charAt(1);
                if (firstChar >= '1' && firstChar <= '8') {
                    return firstChar - '0';
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        return -1;
    }

    /**
     * 生成完整的 Redis key
     * 
     * @param prefix key 前缀，如 "seckill:item:123"
     * @param digit 1-8 的数字
     * @return 完整的 Redis key，如 "seckill:item:123_d1"
     */
    public String generateKey(String prefix, int digit) {
        return prefix + generateKeySuffixByDigit(digit);
    }

    /**
     * 根据前缀生成所有 8 个数字对应的 Redis key 列表
     * 
     * @param prefix key 前缀，如 "seckill:item:123"
     * @return 包含 8 个 key 的列表，按数字 1-8 顺序排列
     */
    public List<String> generateAllKeys(String prefix) {
        return ALL_DIGIT_SUFFIXES.stream()
                .map(digit -> generateKey(prefix, digit))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有数字后缀集合
     * 
     * @return 不可修改的数字后缀列表 [1, 2, 3, 4, 5, 6, 7, 8]
     */
    public List<Integer> getAllDigitSuffixes() {
        return ALL_DIGIT_SUFFIXES;
    }

    /**
     * CRC16 实现（Redis Cluster 使用的算法）
     */
    private static class CRC16 {
        private static final int POLY = 0x1021;
        private int crc = 0;

        public void update(byte[] bytes) {
            for (byte b : bytes) {
                crc ^= (b & 0xFF) << 8;
                for (int i = 0; i < 8; i++) {
                    if ((crc & 0x8000) != 0) {
                        crc = (crc << 1) ^ POLY;
                    } else {
                        crc <<= 1;
                    }
                }
            }
            crc &= 0xFFFF;
        }

        public long getValue() {
            return crc;
        }
    }
}