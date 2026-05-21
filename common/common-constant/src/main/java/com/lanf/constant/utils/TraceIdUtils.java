package com.lanf.constant.utils;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * MDC 链路追踪工具类
 * <p>用于生成分布式链路追踪 ID，方便日志排查和问题定位</p>
 *
 * @author system
 * @since 2024-01-15
 */
public class TraceIdUtils {

    /**
     * MDC 中链路 ID 的键名
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 生成并设置链路 ID
     * <p>使用 UUID 去除横杠的格式作为链路 ID</p>
     */
    public static void generateAndSetTraceId(String key) {

        String traceId = generateTraceId();
        MDC.put(TRACE_ID_KEY, key+":"+traceId);
    }

    public static void setTraceId(String traceId) {

        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 获取当前链路 ID
     *
     * @return 链路 ID，如果不存在则返回 null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清除链路 ID
     * <p>必须在请求结束时调用，防止线程池场景下的内存泄漏</p>
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 清除所有 MDC 上下文
     * <p>推荐使用此方法清理所有 MDC 数据</p>
     */
    public static void clearAll() {
        MDC.clear();
    }


    public static String generateTraceId() {
        return
                UUID.randomUUID().toString().
                        replace("-", "").substring(0,20);
    }
}
