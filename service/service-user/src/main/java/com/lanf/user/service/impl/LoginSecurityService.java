package com.lanf.user.service.impl;

import com.lanf.cache.constant.RedisCacheConstants;
import com.lanf.cache.service.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登入频率控制
 */
@Component
public class LoginSecurityService {


    private final  int MAX_FAIL_COUNT = 100000;

    @Autowired
    private RedisCache redisCache;


    public void handleFailedLogin(String phoneNumber) {


        String key = RedisCacheConstants.getLOGIN_FAIL_COUNT(phoneNumber);

        Long userCount = null;

        if ( !redisCache.hasKey(key)){
            userCount = redisCache.increment(key);
            redisCache.expire(key, RedisCacheConstants.LOGIN_FAIL_COUNT_TIME);

        } else {
            userCount = redisCache.increment(key);

        }

    }
    
    public boolean isLocked(String phoneNumber) {
        String key = RedisCacheConstants.getLOGIN_FAIL_COUNT(phoneNumber);

        Long increment = redisCache.increment(key);

        if ( !redisCache.hasKey(key)){

            return false;
        }

        return increment-1 > MAX_FAIL_COUNT;
    }
}