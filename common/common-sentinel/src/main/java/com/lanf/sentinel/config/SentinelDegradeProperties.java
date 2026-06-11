package com.lanf.sentinel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 熔断降级配置属性
 * <p>
 * 配置示例：
 * <pre>
 * sentinel:
 *   degrade:
 *     enabled: true
 *     rules:
 *       - resource: goods-service
 *         grade: 1
 *         count: 0.5
 *         timeWindow: 10
 *         minRequestAmount: 5
 *         statIntervalMs: 1000
 * </pre>
 *
 * @author qoder
 */
@Data
@ConfigurationProperties(prefix = "sentinel.degrade")
public class SentinelDegradeProperties {

    /**
     * 是否启用熔断降级，默认 true
     */
    private boolean enabled = true;

    /**
     * 熔断规则列表
     */
    private List<DegradeRuleItem> rules = new ArrayList<>();

    @Data
    public static class DegradeRuleItem {

        /**
         * 资源名称（支持方法名或自定义名称）
         */
        private String resource;

        /**
         * 熔断策略：
         * <ul>
         *     <li>0 - 慢调用比例 (SLOW_REQUEST_RATIO)</li>
         *     <li>1 - 异常比例 (ERROR_RATIO)</li>
         *     <li>2 - 异常数 (ERROR_COUNT)</li>
         * </ul>
         */
        private int grade = 1;

        /**
         * 阈值：
         * <ul>
         *     <li>慢调用比例：慢调用阈值（响应时间阈值，单位毫秒）</li>
         *     <li>异常比例：0.0 ~ 1.0 之间的小数</li>
         *     <li>异常数：正整数</li>
         * </ul>
         */
        private double count = 0.5;

        /**
         * 熔断时长（秒），进入熔断状态后多久恢复，默认 10 秒
         */
        private int timeWindow = 10;

        /**
         * 触发熔断的最小请求数，默认 5
         */
        private int minRequestAmount = 5;

        /**
         * 慢调用比例阈值（仅慢调用比例模式有效），默认 1.0
         */
        private double slowRatioThreshold = 1.0;

        /**
         * 统计时长（毫秒），默认 1000ms
         */
        private int statIntervalMs = 1000;
    }
}
