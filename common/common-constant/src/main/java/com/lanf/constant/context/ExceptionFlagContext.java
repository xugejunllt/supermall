package com.lanf.constant.context;

/**
 * 异常标记上下文
 * <p>通过 ThreadLocal 在当前线程中透传异常标记，用于跨方法/跨层级的异常状态传递</p>
 */
public class ExceptionFlagContext {

    private static final ThreadLocal<Boolean> EXCEPTION_FLAG = new ThreadLocal<>();

    /**
     * 设置异常标记为 true
     */
    public static void setException() {
        EXCEPTION_FLAG.set(Boolean.TRUE);
    }

    /**
     * 设置异常标记
     *
     * @param flag 异常标记，true 表示存在异常
     */
    public static void setException(boolean flag) {
        EXCEPTION_FLAG.set(flag);
    }

    /**
     * 获取当前线程的异常标记
     *
     * @return true 表示存在异常，false 或 null 表示无异常
     */
    public static Boolean getExceptionFlag() {
        return EXCEPTION_FLAG.get();
    }

    /**
     * 判断当前线程是否存在异常标记
     *
     * @return true 表示存在异常
     */
    public static boolean hasException() {
        return Boolean.TRUE.equals(EXCEPTION_FLAG.get());
    }

    /**
     * 清除当前线程的异常标记
     * <p>必须在请求结束时调用，防止线程池场景下的内存泄漏</p>
     */
    public static void clear() {
        EXCEPTION_FLAG.remove();
    }
}
