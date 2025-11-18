package com.lanf.user.service.impl;

import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginSecurityService {


    private final  int MAX_FAIL_COUNT = 10;

    @Autowired
    private RedisCache redisCache;


    public void handleFailedLogin(String phoneNumber) {


        String key = CacheConstants.getLOGIN_FAIL_COUNT(phoneNumber);

        Long userCount = null;

        if (redisCache.hasKey(key)){
            userCount = redisCache.increment(key);
            redisCache.expire(key, CacheConstants.LOGIN_FAIL_COUNT_TIME);

        } else {
            userCount = redisCache.increment(key);

        }

    }
    
    public boolean isLocked(String phoneNumber) {
        String key = CacheConstants.getLOGIN_FAIL_COUNT(phoneNumber);

        if ( !redisCache.hasKey(key)){

            return false;
        }
        long cacheObject = Long.parseLong(redisCache.getCacheObject(key));

        return cacheObject <= MAX_FAIL_COUNT;
    }
}