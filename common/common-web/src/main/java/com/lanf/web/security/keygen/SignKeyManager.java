package com.lanf.web.security.keygen;


import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.exception.BizException;
import com.lanf.web.security.encrypt.AesEncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 签名密钥管理器
 * 专门管理用于签名验签的AES密钥
 */
@Slf4j
@Component
public class SignKeyManager {

    /**
     * 签名密钥缓存前缀
     */
    private static final String SIGN_KEY_CACHE_PREFIX = "sign:key:%s";

    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRE_TIME = 10000L;

    /**
     * 缓存过期时间单位
     */
    private static final TimeUnit CACHE_EXPIRE_UNIT = TimeUnit.MINUTES;

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 生成签名密钥并缓存
     * 
     * @return SignKeyInfo 包含随机key和AES密钥（Base64编码）
     */
    public SignKeyInfo generateSignKey() {
        //1.生成AES密钥（16字节=128位）
        byte[] aesKeyBytes = AesEncryptUtils.generateAesKey();
        String aesKeyBase64 = java.util.Base64.getEncoder().encodeToString(aesKeyBytes);
        
        //2.生成随机key作为缓存标识（32位UUID）
        String signRandomKey = UUID.randomUUID().toString().replace("-", "");
        
        //3.将AES密钥缓存到Redis
        String cacheKey = String.format(SIGN_KEY_CACHE_PREFIX, signRandomKey);
        redissonCacheService.set(cacheKey, aesKeyBase64, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        
        log.info("生成签名密钥成功,signRandomKey:[{}],过期时间:[{}]分钟", signRandomKey, CACHE_EXPIRE_TIME);
        
        return new SignKeyInfo(signRandomKey, aesKeyBase64);
    }

    /**
     * 根据随机key获取签名密钥（AES密钥，Base64编码字符串）
     * 
     * @param signRandomKey 签名随机key
     * @return AES密钥（Base64编码）
     */
    private String getSignKey(String signRandomKey) {
        //1.校验参数是否为空
        if (signRandomKey == null || signRandomKey.isEmpty()) {
            throw new BizException("签名随机key不能为空");
        }

        //2.构建缓存key
        String cacheKey = String.format(SIGN_KEY_CACHE_PREFIX, signRandomKey);
        
        //3.从Redis获取AES密钥（Base64编码）
        String aesKeyBase64 = redissonCacheService.get(cacheKey);
        
        //4.校验密钥是否存在
        if (aesKeyBase64 == null || aesKeyBase64.isEmpty()) {
            log.warn("签名密钥不存在或已过期,signRandomKey:[{}]", signRandomKey);
            throw new BizException("签名密钥不存在或已过期");
        }
        
        log.debug("获取签名密钥成功,signRandomKey:[{}]", signRandomKey);
        return aesKeyBase64;
    }

    /**
     * 根据随机key获取签名密钥（AES密钥，解码后的字节数组）
     * 
     * @param signRandomKey 签名随机key
     * @return AES密钥字节数组
     */
    public byte[] getSignKeyBytes(String signRandomKey) {
        //1.获取Base64编码的AES密钥
        String aesKeyBase64 = getSignKey(signRandomKey);
        
        //2.Base64解码为字节数组
        try {
            byte[] aesKeyBytes = java.util.Base64.getDecoder().decode(aesKeyBase64);
            log.debug("签名密钥解码成功,长度:[{}]字节", aesKeyBytes.length);
            return aesKeyBytes;
        } catch (Exception e) {
            log.error("签名密钥Base64解码失败,signRandomKey:[{}]", signRandomKey, e);
            throw new BizException("签名密钥解码失败");
        }
    }

    /**
     * 签名密钥信息
     */
    public static class SignKeyInfo {
        /**
         * 签名随机key（用于从Redis获取密钥）
         */
        private String signRandomKey;
        
        /**
         * AES密钥（Base64编码）
         */
        private String aesKeyBase64;

        public SignKeyInfo() {
        }

        public SignKeyInfo(String signRandomKey, String aesKeyBase64) {
            this.signRandomKey = signRandomKey;
            this.aesKeyBase64 = aesKeyBase64;
        }

        public String getSignRandomKey() {
            return signRandomKey;
        }

        public void setSignRandomKey(String signRandomKey) {
            this.signRandomKey = signRandomKey;
        }

        public String getAesKeyBase64() {
            return aesKeyBase64;
        }

        public void setAesKeyBase64(String aesKeyBase64) {
            this.aesKeyBase64 = aesKeyBase64;
        }
    }
}
