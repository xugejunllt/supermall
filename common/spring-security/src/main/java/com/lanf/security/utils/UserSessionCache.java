package com.lanf.security.utils;

import com.lanf.constant.exception.IRedisException;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 用户 session 缓存管理
 */
@Slf4j
@Component
public class UserSessionCache {

    @Autowired
    private RedisCache redisCache;


    public String getSession(Integer channel, Long userId)  {

        String sessionKey = CacheConstants.getUSER_SESSION_KEY(channel,userId);

        return redisCache.getCacheObject(sessionKey);
    }

    public  void cacheSession(Integer channel, Long userId, String session)  {

        String sessionKey = CacheConstants.getUSER_SESSION_KEY(channel,userId);

        redisCache.setCacheObject(sessionKey, session, CacheConstants.USER_SESSION_TIME);

    }

    public Boolean refreshSession(Integer channel, Long userId)  {

        String sessionKey = CacheConstants.getUSER_SESSION_KEY(channel,userId);
       return redisCache.expire(sessionKey,CacheConstants.USER_SESSION_TIME);

    }

}
