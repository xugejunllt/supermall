package com.lanf.bizcache.util;

import com.lanf.bizcache.model.bo.PlatformRateConfigBO;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BizCacheUtils {

    public static void addCache(List<PlatformRateConfigBO> platformRateConfigBOList) {

        String jsonString = JsonUtils.toJsonString(platformRateConfigBOList);
        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);
        redisCache.setCacheObject(CacheConstants.RATE_CONFIG, jsonString);

    }

    public static void addCache(Long platformId) {

        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);
        redisCache.setCacheObject(CacheConstants.PLATFORM_BUS_ID, platformId);

    }

    public static PlatformRateConfigBO getByType(Integer type) {

        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);

        String cacheObject = redisCache.getCacheObject(CacheConstants.RATE_CONFIG);

        List<PlatformRateConfigBO> configBOS = JsonUtils.toList(cacheObject, PlatformRateConfigBO.class);
        Map<Integer, PlatformRateConfigBO> configMap = configBOS.stream()
                .collect(Collectors.toMap(PlatformRateConfigBO::getType, Function.identity()));

        return configMap.get(type);

    }

    public static Long getPlatformBusId() {

        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);
        return redisCache.getCacheObject(CacheConstants.PLATFORM_BUS_ID);
    }

}
