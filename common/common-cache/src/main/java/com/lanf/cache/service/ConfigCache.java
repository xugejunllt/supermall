package com.lanf.cache.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 简单的 Caffeine 本地缓存工具（支持过期+大小淘汰）
 */
@Component
public class ConfigCache {

    @Resource(name = "cacheManager")
    private CacheManager cacheManager;

    private static Cache config;

    @PostConstruct
    public void init(){
         config = cacheManager.getCache("config");
    }


    /**
     * 存入缓存
     */
    public static void put(String key, Object value) {
        config.put(key, value);
    }

    /**
     * 获取缓存（若不存在返回 null）
     */
    public static <T>T   get(String key,Class<T> type) {

      return   config.get(key, type);

    }

    /**
     * 移除单个 key
     */
    public void remove(String key) {
        config.evict(key);
    }


}