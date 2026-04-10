package com.lanf.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedissonCacheService {

    @Autowired
    private RedissonClient redissonClient;


    public  void set(String key, String value, long expireTime, TimeUnit timeUnit) {
        
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            
            if (expireTime > 0) {
                bucket.set(value, expireTime, timeUnit);
                log.debug("设置缓存:key={},expire={}{}", key, expireTime, timeUnit);
            } else {
                bucket.set(value);
                log.debug("设置缓存(无过期):key={}", key);
            }
        } catch (Exception e) {
            log.error("设置缓存异常:key={}", key, e);
        }
    }

    public String  get(String key) {
        
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String value = bucket.get();
            
            if (value != null) {
                log.debug("缓存命中:key={}", key);
            } else {
                log.debug("缓存未命中:key={}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("获取缓存异常:key={}", key, e);
            return null;
        }
    }


    public void delete(String key) {
        
        try {
            boolean deleted = redissonClient.getBucket(key).delete();
            if (deleted) {
                log.debug("删除缓存成功:key={}", key);
            }
        } catch (Exception e) {
            log.error("删除缓存异常:key={}", key, e);

        }
    }


}
