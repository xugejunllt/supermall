package com.lanf.common.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class StackTraceUtil {
    
    /**
     * 使用 Apache Commons Lang 转换异常堆栈为字符串
     */
    public static String getStackTrace(Throwable throwable) {
        return ExceptionUtils.getStackTrace(throwable);
    }
    
    /**
     * 获取异常的根原因消息
     */
    public static String getRootCauseMessage(Throwable throwable) {
        return ExceptionUtils.getRootCauseMessage(throwable);
    }
}