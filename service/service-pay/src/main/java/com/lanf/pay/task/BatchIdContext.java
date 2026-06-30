package com.lanf.pay.task;

/**
 * 批次号 ThreadLocal 上下文
 */
public class BatchIdContext {

    private static final ThreadLocal<String> BATCH_ID_HOLDER = new ThreadLocal<>();

    public static void setBatchId(String batchId) {
        BATCH_ID_HOLDER.set(batchId);
    }

    public static String getBatchId() {
        return BATCH_ID_HOLDER.get();
    }

    public static void clear() {
        BATCH_ID_HOLDER.remove();
    }
}
