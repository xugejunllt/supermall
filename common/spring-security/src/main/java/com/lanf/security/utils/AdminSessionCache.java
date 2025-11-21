package com.lanf.security.utils;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.security.model.CacheSessionBO;
import com.lanf.system.model.bo.SysUserBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 用户 session 缓存管理
 */
@Slf4j
@Component
public class AdminSessionCache {

    @Autowired
    private RedisCache redisCache;


    public String getToken(Integer channel, Long userId) {

        String tokenKey = CacheConstants.getADMIN_USER_TOKEN(channel, userId);

        return redisCache.getCacheObject(tokenKey);
    }

    public  String getAuth(Integer channel, Long userId) {

        String authKey = CacheConstants.getADMIN_AUTH(channel, userId);


        return redisCache.getCacheObject(authKey);
    }


    public Boolean refreshToken(Integer channel, Long userId) {

        String tokenKey = CacheConstants.getADMIN_USER_TOKEN(channel, userId);
        String refreshTokenKey = CacheConstants.getADMIN_USER_REFRESH_TOKEN(channel, userId);
        String authKey = CacheConstants.getADMIN_AUTH(channel, userId);
        String adminInfo = CacheConstants.getADMIN_INFO(userId);

        Boolean expire = redisCache.expire(tokenKey, CacheConstants.ADMIN_TOKEN_EXP_TIME);
        Boolean expire2 = redisCache.expire(refreshTokenKey, CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);
        Boolean expire3 = redisCache.expire(authKey, CacheConstants.ADMIN_ADMIN_AUTH_EXP_TIME);
        Boolean expire4 = redisCache.expire(adminInfo, CacheConstants.ADMIN_USER_INFO_EXP_TIME);

        return expire && expire2 && expire3 && expire4;

    }

    public static SysUserBO getSysUser() {

        Long userId = UserIdContext.getUserId();
        String adminInfo = CacheConstants.getADMIN_INFO(userId);
        RedisCache redisCache1 = BeanUtil.getBean(RedisCache.class);
        String cacheObject = redisCache1.getCacheObject(adminInfo);

        return JsonUtils.toObject(cacheObject, SysUserBO.class);

    }
    public SysUserBO getSysUser(Long userId) {

        String adminInfo = CacheConstants.getADMIN_INFO(userId);
        String cacheObject = redisCache.getCacheObject(adminInfo);

        return JsonUtils.toObject(cacheObject, SysUserBO.class);

    }
    public CacheSessionBO cacheSession(SysUserBO sysUser, Collection<GrantedAuthority> authorities) {


        Integer channel = sysUser.getChannel();
        Long userId = sysUser.getId();
        String deviceId = sysUser.getDeviceId();
        String tokenKey = CacheConstants.getADMIN_USER_TOKEN(channel, userId);
        String refreshTokenKey = CacheConstants.getADMIN_USER_REFRESH_TOKEN(channel, userId);

        String token = JwtUtils.createUserToken(userId, deviceId, CacheConstants.ADMIN_TOKEN_EXP_TIME);
        String refreshToken = JwtUtils.createUserToken(userId, deviceId, CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);

        redisCache.setCacheObject(tokenKey, token, CacheConstants.ADMIN_TOKEN_EXP_TIME);
        redisCache.setCacheObject(refreshTokenKey, refreshToken, CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);

        //添加user权限
        String authKey = CacheConstants.getADMIN_AUTH(channel, userId);
        String authJsonString = JsonUtils.toJsonString(authorities);
        redisCache.setCacheObject(authKey, authJsonString, CacheConstants.ADMIN_ADMIN_AUTH_EXP_TIME);

        /**
         * 添加用户缓存
         */
        String adminInfo = CacheConstants.getADMIN_INFO(userId);
        redisCache.setCacheObject(adminInfo, JsonUtils.toJsonString(sysUser), CacheConstants.ADMIN_USER_INFO_EXP_TIME);


        CacheSessionBO cacheSessionBO = new CacheSessionBO();
        cacheSessionBO.setToken(token);
        cacheSessionBO.setRefreshToken(refreshToken);
        return cacheSessionBO;
    }

    public void cleanSession(SysUserBO sysUser1) {

        Integer channel = sysUser1.getChannel();
        String  deviceId = sysUser1.getDeviceId();
        Long userId = sysUser1.getId();
        String token = JwtUtils.createUserToken(userId, deviceId, CacheConstants.ADMIN_TOKEN_EXP_TIME);
        String refreshToken = JwtUtils.createUserToken(userId, deviceId, CacheConstants.ADMIN_REFRESH_TOKEN_EXP_TIME);
        String authKey = CacheConstants.getADMIN_AUTH(channel, userId);
        String adminInfo = CacheConstants.getADMIN_INFO(userId);

        redisCache.deleteObject(token);
        redisCache.deleteObject(refreshToken);
        redisCache.deleteObject(authKey);
        redisCache.deleteObject(adminInfo);
    }


}
