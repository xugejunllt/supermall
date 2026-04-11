package com.lanf.pay.service.trade.impl;

import com.lanf.cache.constant.RedisCacheConstants;
import com.lanf.cache.service.ConfigCache;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PayCompensateOrderRetryPolicyBO;
import com.lanf.pay.service.trade.IPayCompensateOrderRetryPolicyService;
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
    private IPayCompensateOrderRetryPolicyService payCompensateOrderRetryPolicyService;

    private static final String LOCAL_CACHE_KEY = "pay_retry_policy_list";

    private static final String REDIS_CACHE_KEY = RedisCacheConstants.PAY_RETRY_POLICY;

    private static final String FIRST_LEVEL_RETRY_LOCAL_CACHE_KEY = "pay_first_level_retry_policy";


    public List<PayCompensateOrderRetryPolicyBO> getAllRetryPolicies() {

        List<PayCompensateOrderRetryPolicyBO> policies = getFromLocalCache();

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

    public PayCompensateOrderRetryPolicyBO getFirstLevelRetryPolicy() {

        PayCompensateOrderRetryPolicyBO firstLevelPolicy = getFirstLevelFromLocalCache();

        if (firstLevelPolicy != null) {
            log.debug("本地缓存命中：一级重试策略");
            return firstLevelPolicy;
        }

        log.debug("本地缓存未命中，从所有策略中筛选一级重试策略");
        List<PayCompensateOrderRetryPolicyBO> allPolicies = getAllRetryPolicies();

        if (allPolicies != null && !allPolicies.isEmpty()) {
            firstLevelPolicy = filterFirstLevelRetryPolicy(allPolicies);

            if (firstLevelPolicy != null) {
                log.debug("筛选到一级重试策略：retryLevel={}, delaySeconds={}", 
                        firstLevelPolicy.getRetryLevel(), firstLevelPolicy.getDelaySeconds());
                putFirstLevelToLocalCache(firstLevelPolicy);
            } else {
                log.warn("未找到一级重试策略配置");
            }
        }

        return firstLevelPolicy;
    }

    private PayCompensateOrderRetryPolicyBO getFirstLevelFromLocalCache() {

        return configCache.get(FIRST_LEVEL_RETRY_LOCAL_CACHE_KEY, PayCompensateOrderRetryPolicyBO.class);

    }

    private void putFirstLevelToLocalCache(PayCompensateOrderRetryPolicyBO policy) {

        configCache.put(FIRST_LEVEL_RETRY_LOCAL_CACHE_KEY, policy);

    }

    private PayCompensateOrderRetryPolicyBO filterFirstLevelRetryPolicy(List<PayCompensateOrderRetryPolicyBO> policies) {

        if (policies == null || policies.isEmpty()) {
            return null;
        }

        return policies.stream()
                .filter(policy -> policy.getRetryLevel() != null && policy.getRetryLevel() == 1)
                .findFirst()
                .orElse(null);

    }

    private List<PayCompensateOrderRetryPolicyBO> getFromLocalCache() {


        return configCache.get(LOCAL_CACHE_KEY, List.class);

    }

    private void putToLocalCache(List<PayCompensateOrderRetryPolicyBO> policies) {


        configCache.put(LOCAL_CACHE_KEY, policies);

    }

    @SuppressWarnings("unchecked")
    private List<PayCompensateOrderRetryPolicyBO> getFromRedisCache() {


        String jsonValue = redissonCacheService.get(REDIS_CACHE_KEY);

        if (jsonValue == null || jsonValue.isEmpty()) {
            return null;
        }

        return JsonUtils.toList(jsonValue, PayCompensateOrderRetryPolicyBO.class);

    }

    private void putToRedisCache(List<PayCompensateOrderRetryPolicyBO> policies) {


        String jsonValue = JsonUtils.toJsonString(policies);
        redissonCacheService.set(REDIS_CACHE_KEY, jsonValue, RedisCacheConstants.PAY_RETRY_POLICY_EXP_TIME, TimeUnit.MINUTES);


    }

    private List<PayCompensateOrderRetryPolicyBO> loadFromDB() {

        return  payCompensateOrderRetryPolicyService.getRetryPolicy();

    }






}
