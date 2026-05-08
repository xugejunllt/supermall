package com.lanf.seckill.service.strategy;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecKillResultCache {

    @Autowired
    private RedissonCacheService redissonCacheService;

    private static final String SEC_KILL_RESULT_KEY_PREFIX = "seckill:result:%s:%s";

    private static final long CACHE_EXPIRE_DAYS = 3;


    public void addResult(Long userId, Long secKillItemId, SecKillResultEnum result) {

        String key = String.format(SEC_KILL_RESULT_KEY_PREFIX, userId, secKillItemId);

        redissonCacheService.set(key, result.getCode().toString(), CACHE_EXPIRE_DAYS,
                java.util.concurrent.TimeUnit.DAYS);
    }

    public SecKillResultEnum getResult(Long userId, Long secKillItemId) {

        String key = String.format(SEC_KILL_RESULT_KEY_PREFIX, userId, secKillItemId);

        String result = redissonCacheService.get(key);

        if (result == null) {
            return SecKillResultEnum.SOLD_OUT;
        }

        return SecKillResultEnum.getByCode(Integer.parseInt(result));
    }

}
