package com.lanf.user.constant;

/**
 * Redis Key 常量类
 * 统一管理所有 Redis Key 格式
 */
public class UserRedisKeyConstants {



    // ==================== 验证码相关 ====================

    /**
     * 注册验证码
     * 格式: user:register:code:{phoneNumber}
     */
    public static final String REGISTER_CODE_KEY = "user:register:code:%s";

    /**
     * 登录验证码
     * 格式: user:login:code:{phoneNumber}
     */
    public static final String LOGIN_CODE_KEY = "user:login:code:%s";

}
