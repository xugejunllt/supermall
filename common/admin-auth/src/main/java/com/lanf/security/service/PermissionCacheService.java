package com.lanf.security.service;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜单列表权限
 * 用户权限缓存服务
 * 负责将用户权限列表缓存到 Redis，并在鉴权时从缓存中读取
 */
@Slf4j
@Component
public class PermissionCacheService {

    private static final String PERMISSION_CACHE_KEY_PREFIX = "admin:permissions:%s:%s";

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 缓存用户权限到 Redis
     * 
     * @param userId 用户ID
     * @param channel 渠道标识
     * @param authorities 权限列表
     * @param expireTime 过期时间（分钟）
     */
    public void cachePermissions(Long userId, Integer channel, Collection<GrantedAuthority> authorities, long expireTime) {
        if (userId == null || channel == null || authorities == null) {
            log.warn("缓存用户权限失败：参数为空, userId={}, channel={}", userId, channel);
            return;
        }

        try {
            // 提取权限字符串列表
            List<String> perms = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // 构建 Redis Key
            String key = buildPermissionKey(userId, channel);

            // 缓存权限列表
            redissonCacheService.set(key, JsonUtils.toJsonString(perms), expireTime, TimeUnit.MINUTES);

            log.debug("用户权限已缓存到 Redis, userId={}, channel={}, permsCount={}, expireTime={}min", 
                    userId, channel, perms.size(), expireTime);

        } catch (Exception e) {
            log.error("缓存用户权限失败, userId={}, channel={}", userId, channel, e);
        }
    }

    /**
     * 从 Redis 缓存中获取用户权限
     * 
     * @param userId 用户ID
     * @param channel 渠道标识
     * @return 权限列表，如果缓存不存在则返回空列表
     */
    public List<GrantedAuthority> getPermissions(Long userId, String channel) {
        if (userId == null || channel == null) {
            log.warn("获取用户权限失败：参数为空, userId={}, channel={}", userId, channel);
            return Collections.emptyList();
        }

        try {
            // 构建 Redis Key
            String key = buildPermissionKey(userId, channel);

            // 从 Redis 获取权限列表
            List<String> perms = JsonUtils.toList(redissonCacheService.get(key), String.class);

            if (perms == null || perms.isEmpty()) {
                log.debug("用户权限缓存不存在，可能需要重新登录, userId={}, channel={}", userId, channel);
                return Collections.emptyList();
            }

            // 转换为 GrantedAuthority 列表
            List<GrantedAuthority> authorities = perms.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            log.debug("从缓存加载用户权限, userId={}, channel={}, permsCount={}", 
                    userId, channel, authorities.size());

            return authorities;

        } catch (Exception e) {
            log.error("从缓存加载用户权限失败, userId={}, channel={}", userId, channel, e);
            return Collections.emptyList();
        }
    }

    /**
     * 清除用户权限缓存
     * 
     * @param userId 用户ID
     * @param channel 渠道标识
     */
    public void clearPermissions(Long userId, Integer channel) {
        if (userId == null || channel == null) {
            return;
        }

        try {
            String key = buildPermissionKey(userId, channel);
            redissonCacheService.delete(key);
            log.info("用户权限缓存已清除, userId={}, channel={}", userId, channel);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败, userId={}, channel={}", userId, channel, e);
        }
    }

    /**
     * 检查用户权限缓存是否存在
     * 
     * @param userId 用户ID
     * @param channel 渠道标识
     * @return true-存在，false-不存在
     */
    public boolean hasPermissions(Long userId, Integer channel) {
        if (userId == null || channel == null) {
            return false;
        }

        try {
            String key = buildPermissionKey(userId, channel);
            return redissonCacheService.exists(key);
        } catch (Exception e) {
            log.error("检查用户权限缓存失败, userId={}, channel={}", userId, channel, e);
            return false;
        }
    }

    /**
     * 构建权限缓存 Key
     * 
     * @param userId 用户ID
     * @param channel 渠道标识
     * @return Redis Key
     */
    private String buildPermissionKey(Long userId, Object channel) {
        return String.format( PERMISSION_CACHE_KEY_PREFIX, userId, channel);
    }
}
