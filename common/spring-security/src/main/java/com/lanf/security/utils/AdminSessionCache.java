package com.lanf.security.utils;

import com.lanf.common.utils.JsonUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.security.model.CacheSessionBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * 用户 session 缓存管理
 */
@Slf4j
@Component
public class AdminSessionCache {

    @Autowired
    private RedisCache redisCache;


    public String getToken(Integer channel, Long userId)  {

        String tokenKey = CacheConstants.getADMIN_USER_TOKEN(channel,userId);

        return redisCache.getCacheObject(tokenKey);
    }

    public String getAuth(Integer channel, Long userId){

        String authKey = CacheConstants.getADMIN_AUTH( channel,userId);


        return  redisCache.getCacheObject(authKey);
    }




    public Boolean refreshToken(Integer channel, Long userId)  {

        String tokenKey = CacheConstants.getADMIN_USER_TOKEN(channel,userId);
        String refreshTokenKey = CacheConstants.getADMIN_USER_REFRESH_TOKEN(channel, userId);
        String authKey = CacheConstants.getADMIN_AUTH( channel,userId);

        Boolean expire = redisCache.expire(tokenKey, CacheConstants.ADMIN_TOKEN_EXP_TIME);
        Boolean expire2 = redisCache.expire(refreshTokenKey, CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);
        Boolean expire3 = redisCache.expire(authKey, CacheConstants.ADMIN_ADMIN_AUTH_EXP_TIME);

        return expire && expire2 && expire3;

    }

    public CacheSessionBO cacheSession(Integer channel, Long userId, String deviceId, String username,Long merchantId,
                                       Collection<GrantedAuthority> authorities)  {

        String tokenKey = CacheConstants.getADMIN_USER_TOKEN(channel,userId);
        String refreshTokenKey = CacheConstants.getADMIN_USER_REFRESH_TOKEN(channel, userId);

        String token = JwtUtils.createUserToken(userId,deviceId,username,merchantId,CacheConstants.ADMIN_TOKEN_EXP_TIME);
        String refreshToken = JwtUtils.createUserToken(userId,deviceId,username,merchantId,CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);

        redisCache.setCacheObject(tokenKey, token, CacheConstants.ADMIN_TOKEN_EXP_TIME);
        redisCache.setCacheObject(refreshTokenKey, refreshToken, CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);

        //添加user权限
        String authKey = CacheConstants.getADMIN_AUTH( channel,userId);
        String authJsonString = JsonUtils.toJsonString(authorities);
        redisCache.setCacheObject(authKey, authJsonString, CacheConstants.ADMIN_ADMIN_AUTH_EXP_TIME);


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
