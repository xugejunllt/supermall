package com.lanf.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
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

    public void addToSet(String key, String value, long expireTime, TimeUnit timeUnit) {
        
        try {
            RSet<String> set = redissonClient.getSet(key);
            set.add(value);
            
            if (expireTime > 0) {
                set.expire(expireTime, timeUnit);
                log.debug("添加到Set缓存:key={},value={},expire={}{}", key, value, expireTime, timeUnit);
            } else {
                log.debug("添加到Set缓存(无过期):key={},value={}", key, value);
            }
        } catch (Exception e) {
            log.error("添加到Set缓存异常:key={},value={}", key, value, e);
        }
    }

    public Set<String> getSetMembers(String key) {
        
        try {
            RSet<String> set = redissonClient.getSet(key);
            Set<String> members = set.readAll();
            
            if (members != null && !members.isEmpty()) {
                log.debug("获取Set缓存命中:key={},size={}", key, members.size());
            } else {
                log.debug("获取Set缓存未命中:key={}", key);
            }
            return members;
        } catch (Exception e) {
            log.error("获取Set缓存异常:key={}", key, e);
            return null;
        }
    }

    public boolean isMemberOfSet(String key, String value) {
        
        try {
            RSet<String> set = redissonClient.getSet(key);
            boolean isMember = set.contains(value);
            
            if (isMember) {
                log.debug("Set缓存成员存在:key={},value={}", key, value);
            } else {
                log.debug("Set缓存成员不存在:key={},value={}", key, value);
            }
            return isMember;
        } catch (Exception e) {
            log.error("检查Set缓存成员异常:key={},value={}", key, value, e);
            return false;
        }
    }

    public void addAllToSet(String key, java.util.Collection<String> values, long expireTime, TimeUnit timeUnit) {
        
        try {
            if (values == null || values.isEmpty()) {
                log.debug("批量添加到Set缓存：集合为空,key={}", key);
                return;
            }
            
            RSet<String> set = redissonClient.getSet(key);
            set.addAll(values);
            
            if (expireTime > 0) {
                set.expire(expireTime, timeUnit);
                log.debug("批量添加到Set缓存成功:key={},count={},expire={}{}", key, values.size(), expireTime, timeUnit);
            } else {
                log.debug("批量添加到Set缓存成功(无过期):key={},count={}", key, values.size());
            }
        } catch (Exception e) {
            log.error("批量添加到Set缓存异常:key={},count={}", key, values.size(), e);
        }
    }


}
