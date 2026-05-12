package com.lanf.constant.constant;

/**
 * Redis Key 常量类
 * 统一管理所有 Redis Key 格式
 */
public class RedisKeyConstants {

    // ==================== 用户相关 ====================

    /**
     * 用户访问令牌
     * 格式: user:access:token:{userId}:{channel}
     */
    public static final String USER_ACCESS_TOKEN = "user:access:token:%s:%s";

    /**
     * 用户刷新令牌
     * 格式: user:refresh:token:{userId}:{channel}
     */
    public static final String USER_REFRESH_TOKEN = "user:refresh:token:%s:%s";

    /**
     * 用户访问令牌
     * 格式: admin:access:token:{userId}:{channel}
     */
    public static final String ADMIN_ACCESS_TOKEN = "admin:access:token:%s:%s";

    /**
     * 用户刷新令牌
     * 格式: admin:refresh:token:{userId}:{channel}
     */
    public static final String ADMIN_REFRESH_TOKEN = "admin:refresh:token:%s:%s";

}
