package com.lanf.security.utils;


/**
 * 用户上下文工具类
 * 用于在同一个线程中共享用户信息
 */
public class UserIdContext {
    
    private static final ThreadLocal<Long> USER_ID_CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前线程的用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_CONTEXT.set(userId);
    }
    
    /**
     * 获取当前线程的用户ID
     */
    public static Long getUserId() {
        return USER_ID_CONTEXT.get();
    }
    

    
    /**
     * 清除当前线程的用户ID
     */
    public static void clear() {
        USER_ID_CONTEXT.remove();
    }
    
    /**
     * 检查是否已设置用户ID
     */
    public static boolean hasUserId() {
        return USER_ID_CONTEXT.get() != null;
    }
}