package com.lanf.bizcache.service;

import com.lanf.bizcache.model.bo.PlatformRateConfigBO;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.cache.constant.RedisCacheConstants;
import com.lanf.cache.service.RedisCache;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BizCacheService {

    public static void addCache(List<PlatformRateConfigBO> platformRateConfigBOList) {

        String jsonString = JsonUtils.toJsonString(platformRateConfigBOList);
        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);
        redisCache.setCacheObject(RedisCacheConstants.RATE_CONFIG, jsonString);

    }

    public static void addCache(Long platformId) {

        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);
        redisCache.setCacheObject(RedisCacheConstants.PLATFORM_BUS_ID, platformId);

    }

    public static PlatformRateConfigBO getByType(Integer type) {

        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);

        String cacheObject = redisCache.getCacheObject(RedisCacheConstants.RATE_CONFIG);

        List<PlatformRateConfigBO> configBOS = JsonUtils.toList(cacheObject, PlatformRateConfigBO.class);
        Map<Integer, PlatformRateConfigBO> configMap = configBOS.stream()
                .collect(Collectors.toMap(PlatformRateConfigBO::getType, Function.identity()));

        return configMap.get(type);

    }


}
