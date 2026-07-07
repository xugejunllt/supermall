package com.lanf.rocketmq.aspect;

import com.lanf.rocketmq.model.enums.MqConsumeExceptionTypeEnum;

/**
 * 异常标记上下文
 * <p>通过 ThreadLocal 在当前线程中透传异常类型，用于跨方法/跨层级的异常状态传递</p>
 */
public class ExceptionFlagContext {

    private static final ThreadLocal<MqConsumeExceptionTypeEnum> EXCEPTION_FLAG = new ThreadLocal<>();

    /**
     * 设置异常类型
     *
     * @param exceptionType 异常类型枚举
     */
    public static void setExceptionType(MqConsumeExceptionTypeEnum exceptionType) {
        EXCEPTION_FLAG.set(exceptionType);
    }

    /**
     * 获取当前线程的异常类型
     *
     * @return 异常类型枚举，如果不存在则返回 null
     */
    public static MqConsumeExceptionTypeEnum getExceptionType() {
        return EXCEPTION_FLAG.get();
    }

    /**
     * 判断当前线程是否存在异常标记
     *
     * @return true 表示存在异常
     */
    public static boolean hasException() {
        return EXCEPTION_FLAG.get() != null;
    }

    /**
     * 判断当前异常是否需要重试
     *
     * @return true 表示需要重试
     */
    public static boolean isNeedRetry() {
        return MqConsumeExceptionTypeEnum.NEED_RETRY.equals(EXCEPTION_FLAG.get());
    }

    /**
     * 清除当前线程的异常标记
     * <p>必须在请求结束时调用，防止线程池场景下的内存泄漏</p>
     */
    public static void clear() {
        EXCEPTION_FLAG.remove();
    }
}
