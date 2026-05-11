package com.lanf.web.constant;

/**
 * Web 模块 Redis Key 常量类
 * 统一管理 Web 相关的所有 Redis Key 格式
 */
public class WebRedisKeyConstants {

    // ==================== 签名密钥相关 ====================

    /**
     * 签名密钥缓存
     * 格式: sign:key:{signRandomKey}
     * 用途: 存储用于签名验签的AES密钥（Base64编码）
     * 过期时间: 10分钟
     */
    public static final String SIGN_KEY_CACHE = "sign:key:%s";

    /**
     * 签名随机数（防重放）
     * 格式: sign:nonce:{nonce}
     * 用途: 防止请求重放攻击
     * 过期时间: 5分钟
     */
    public static final String SIGN_NONCE_CACHE = "sign:nonce:%s";

    // ==================== RSA公钥相关 ====================

    /**
     * RSA密钥对缓存
     * 格式: public:key:{randomKey}
     * 用途: 存储RSA密钥对（JSON格式，包含公钥和私钥）
     * 过期时间: 10分钟
     */
    public static final String RSA_KEY_PAIR_CACHE = "public:key:%s";

}
