package com.lanf.seckill.service.strategy;

import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SecKillStrategyFactory {

    @Autowired
    private List<SecKillStrategy> strategies;

    private final Map<Integer, SecKillStrategy> strategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (SecKillStrategy strategy : strategies) {
            strategyMap.put(strategy.getSupportedMode(), strategy);
            log.info("注册秒杀策略: mode={}", strategy.getSupportedMode());
        }
    }

    /**
     * 根据秒杀模式获取策略
     *
     * @param mode 秒杀模式
     * @return 对应的策略实现
     */
    public SecKillStrategy getStrategy(Integer mode) {
        SecKillStrategy strategy = strategyMap.get(mode);
        if (strategy == null) {
            log.error("未找到秒杀模式对应的策略: mode={}", mode);
            throw new BizException("不支持的秒杀模式");
        }
        return strategy;
    }
}
