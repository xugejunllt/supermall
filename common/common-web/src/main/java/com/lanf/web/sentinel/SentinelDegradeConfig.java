package com.lanf.web.sentinel;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sentinel 熔断降级配置类
 * <p>
 * 在应用启动时自动加载熔断规则到 Sentinel。
 * 支持通过配置文件动态定义熔断策略。
 *
 * @author qoder
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SentinelDegradeProperties.class)
public class SentinelDegradeConfig {

    @Autowired
    private SentinelDegradeProperties degradeProperties;

    /**
     * 初始化熔断规则
     */
    @PostConstruct
    public void initDegradeRules() {
        if (!degradeProperties.isEnabled()) {
            log.info("[Sentinel] 熔断降级功能已关闭");
            return;
        }

        List<SentinelDegradeProperties.DegradeRuleItem> rules = degradeProperties.getRules();
        if (rules == null || rules.isEmpty()) {
            log.warn("[Sentinel] 未配置任何熔断规则，请检查配置项 sentinel.degrade.rules");
            return;
        }

        List<DegradeRule> degradeRules = rules.stream()
                .map(this::convertToDegradeRule)
                .collect(Collectors.toList());

        DegradeRuleManager.loadRules(degradeRules);
        log.info("[Sentinel] 已成功加载 {} 条熔断降级规则", degradeRules.size());

        for (DegradeRule rule : degradeRules) {
            log.info("[Sentinel] 熔断规则 => 资源: {}, 策略: {}, 阈值: {}, 熔断时长: {}s, 最小请求数: {}",
                    rule.getResource(),
                    getStrategyName(rule.getGrade()),
                    rule.getCount(),
                    rule.getTimeWindow(),
                    rule.getMinRequestAmount());
        }
    }

    /**
     * 将配置项转换为 Sentinel DegradeRule
     */
    private DegradeRule convertToDegradeRule(SentinelDegradeProperties.DegradeRuleItem item) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(item.getResource());
        rule.setGrade(item.getGrade());
        rule.setCount(item.getCount());
        rule.setTimeWindow(item.getTimeWindow());
        rule.setMinRequestAmount(item.getMinRequestAmount());
        rule.setSlowRatioThreshold(item.getSlowRatioThreshold());
        rule.setStatIntervalMs(item.getStatIntervalMs());
        return rule;
    }

    /**
     * 获取熔断策略名称
     */
    private String getStrategyName(int grade) {
        switch (grade) {
            case 0:
                return "慢调用比例";
            case 1:
                return "异常比例";
            case 2:
                return "异常数";
            default:
                return "未知";
        }
    }
}
