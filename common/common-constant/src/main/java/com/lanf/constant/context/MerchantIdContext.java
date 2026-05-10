package com.lanf.constant.context;


/**
 *
 * 租户id
 */
public class MerchantIdContext {
    
    private static final ThreadLocal<Long> MERCHANENT = new ThreadLocal<>();
    
    /**
     * 设置当前线程的用户ID
     */
    public static void setMerchantId(Long merchantId) {
        MERCHANENT.set(merchantId);
    }
    
    /**
     * 获取当前线程的用户ID
     */
    public static Long getMerchantId() {
        return MERCHANENT.get();
    }

    /**
     * 清除当前线程的用户ID
     */
    public static void clear() {
        MERCHANENT.remove();
    }
    
    /**
     * 检查是否已设置用户ID
     */
    public static boolean hasUserId() {
        return MERCHANENT.get() != null;
    }
}