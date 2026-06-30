package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.api.pay.model.enums.ReconciliationJobTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对账扫描策略工厂
 */
@Slf4j
@Component
public class ReconciliationStrategyFactory {
    
    @Autowired
    private List<ReconciliationStrategy> strategies;
    
    private final Map<ReconciliationJobTypeEnum, ReconciliationStrategy> strategyMap = new HashMap<>();
    
    @PostConstruct
    public void init() {

        for (ReconciliationStrategy strategy : strategies) {

            strategyMap.put(strategy.getJobType(), strategy);
        }
        log.info("初始化当前对账策略:{}", strategyMap);

    }
    
    /**
     * 根据任务类型获取对应的策略
     * @param jobType 任务类型
     * @return 对应的策略实现
     */
    public ReconciliationStrategy getStrategy(ReconciliationJobTypeEnum jobType) {

        log.info("当前jobtype:{}",jobType);
        ReconciliationStrategy strategy = strategyMap.get(jobType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的任务类型: " + jobType);
        }
        return strategy;
    }
}
