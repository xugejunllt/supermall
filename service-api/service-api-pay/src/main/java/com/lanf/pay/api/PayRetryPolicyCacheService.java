package com.lanf.pay.api;

import com.lanf.cache.constant.RedisCacheConstants;
import com.lanf.cache.service.ConfigCache;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.result.Result;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.pay.model.vo.PayCompensateOrderRetryPolicyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PayRetryPolicyCacheService {

    @Autowired
    private ConfigCache configCache;

    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private PayApiService payApiService;

    private static final String LOCAL_CACHE_KEY = "pay_retry_policy_list";

    private static final String REDIS_CACHE_KEY = RedisCacheConstants.PAY_RETRY_POLICY;


    public List<PayCompensateOrderRetryPolicyVO> getAllRetryPolicies() {

        List<PayCompensateOrderRetryPolicyVO> policies = getFromLocalCache();

        if (policies != null && !policies.isEmpty()) {
            log.debug("本地缓存命中：所有重试策略");
            return policies;
        }

        log.debug("本地缓存未命中，从Redis获取");
        policies = getFromRedisCache();

        if (policies != null && !policies.isEmpty()) {
            log.debug("Redis缓存命中");
            putToLocalCache(policies);
            return policies;
        }

        log.debug("Redis缓存未命中，从DB加载");
        policies = loadFromDB();

        if (policies != null && !policies.isEmpty()) {
            log.debug("从DB加载成功：所有重试策略，count={}", policies.size());
            putToRedisCache(policies);
            putToLocalCache(policies);
        } else {
            log.warn("未从DB加载到任何重试策略配置");

        }

        return policies;
    }

    private List<PayCompensateOrderRetryPolicyVO> getFromLocalCache() {


        return configCache.get(LOCAL_CACHE_KEY, List.class);

    }

    private void putToLocalCache(List<PayCompensateOrderRetryPolicyVO> policies) {


        configCache.put(LOCAL_CACHE_KEY, policies);

    }

    @SuppressWarnings("unchecked")
    private List<PayCompensateOrderRetryPolicyVO> getFromRedisCache() {


        String jsonValue = redissonCacheService.get(REDIS_CACHE_KEY);

        if (jsonValue == null || jsonValue.isEmpty()) {
            return null;
        }

        return JsonUtils.toList(jsonValue, PayCompensateOrderRetryPolicyVO.class);

    }

    private void putToRedisCache(List<PayCompensateOrderRetryPolicyVO> policies) {


        String jsonValue = JsonUtils.toJsonString(policies);
        redissonCacheService.set(REDIS_CACHE_KEY, jsonValue, RedisCacheConstants.PAY_RETRY_POLICY_EXP_TIME, TimeUnit.MINUTES);


    }

    private List<PayCompensateOrderRetryPolicyVO> loadFromDB() {


        log.info("通过Feign调用获取所有重试策略");

        Result<List<PayCompensateOrderRetryPolicyVO>> result = payApiService.getRetryPolicy();


        return  RpcResultParser.parseResult( result);

    }






}
