package com.lanf.security.utils;

import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.security.model.CacheSessionBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户 session 缓存管理
 */
@Slf4j
@Component
public class UserSessionCache {

    @Autowired
    private RedisCache redisCache;


    public String getToken(Integer channel, Long userId)  {

        String tokenKey = CacheConstants.getUSER_TOKEN_KEY(channel,userId);

        return redisCache.getCacheObject(tokenKey);
    }

    public String getRefreshToken(Integer channel, Long userId)  {

        String tokenKey = CacheConstants.getUSER_REFRESH_TOKEN(channel,userId);

        return redisCache.getCacheObject(tokenKey);
    }



    public Boolean refreshToken(Integer channel, Long userId)  {

        String tokenKey = CacheConstants.getUSER_TOKEN_KEY(channel,userId);
        String refreshTokenKey = CacheConstants.getUSER_REFRESH_TOKEN(channel, userId);
        Boolean expire = redisCache.expire(tokenKey, CacheConstants.TOKEN_EXP_TIME);
        Boolean expire2 = redisCache.expire(refreshTokenKey, CacheConstants.REFRESH_TOKEN_EXP_TIME);
        return expire && expire2;

    }

    public CacheSessionBO cacheSession(Integer channel, Long userId,String deviceId)  {

        String tokenKey = CacheConstants.getUSER_TOKEN_KEY(channel,userId);
        String refreshTokenKey = CacheConstants.getUSER_REFRESH_TOKEN(channel, userId);

        String token = JwtUtils.createUserToken(userId,deviceId,CacheConstants.TOKEN_EXP_TIME);
        String refreshToken = JwtUtils.createUserToken(userId,deviceId,CacheConstants.REFRESH_TOKEN_EXP_TIME);

        redisCache.setCacheObject(tokenKey, token, CacheConstants.TOKEN_EXP_TIME);
        redisCache.setCacheObject(refreshTokenKey, refreshToken, CacheConstants.REFRESH_TOKEN_EXP_TIME);

        CacheSessionBO cacheSessionBO = new CacheSessionBO();
        cacheSessionBO.setToken(token);
        cacheSessionBO.setRefreshToken(refreshToken);
        return  cacheSessionBO;
    }

    public  void cleanSession(Integer channel, Long userId)  {

        String tokenKey = CacheConstants.getUSER_TOKEN_KEY(channel,userId);
        String refreshTokenKey = CacheConstants.getUSER_REFRESH_TOKEN(channel, userId);
        redisCache.deleteObject(tokenKey);
        redisCache.deleteObject(refreshTokenKey);




    }

















}
