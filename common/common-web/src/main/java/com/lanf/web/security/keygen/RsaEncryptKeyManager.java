package com.lanf.web.security.keygen;


import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.web.security.keygen.model.IKeyPairInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *  公钥管理器
 * 专门管理RSA密钥对
 */
@Slf4j
@Component
public class RsaEncryptKeyManager {

    private static final String PUBLIC_KEY_CACHE_PREFIX = "public:key:%s";

    private static final long CACHE_EXPIRE_TIME = 10;

    private static final TimeUnit CACHE_EXPIRE_UNIT = TimeUnit.MINUTES;

    @Autowired
    @Qualifier("encryptKeyManagerServiceImpl")
    private AbstractKeyManagerService keyManagerService;

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 生成RSA密钥对并缓存
     * 
     * @return PublicKeyInfo 包含随机key和RSA公钥（Base64编码）
     */
    public PublicKeyInfo generatePublicKey() {
        IKeyPairInfo keyPairInfo = keyManagerService.findKeyPairInfo();
        
        String randomKey = UUID.randomUUID().toString().replace("-", "");
        
        String cacheKey = String.format(PUBLIC_KEY_CACHE_PREFIX, randomKey);
        String keyPairJson = JsonUtils.toJsonString(keyPairInfo);
        
        redissonCacheService.set(cacheKey, keyPairJson, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        
        log.info("生成RSA密钥对成功,randomKey:[{}]", randomKey);
        
        return new PublicKeyInfo(randomKey, keyPairInfo.getPublicKey());
    }

    /**
     * 根据随机key获取RSA公钥字节数组
     * 
     * @param randomKey 随机key
     * @return RSA公钥字节数组
     */
    public byte[] getPublicKeyBytes(String randomKey) {
        if (randomKey == null || randomKey.isEmpty()) {
            throw new BizException("随机key不能为空");
        }

        String cacheKey = String.format(PUBLIC_KEY_CACHE_PREFIX, randomKey);
        String keyPairJson = redissonCacheService.get(cacheKey);
        
        if (keyPairJson == null || keyPairJson.isEmpty()) {
            log.warn("密钥对不存在或已过期,randomKey:[{}]", randomKey);
            throw new BizException("密钥对不存在或已过期");
        }
        
        try {
            IKeyPairInfo keyPairInfo = JsonUtils.toObject(keyPairJson, IKeyPairInfo.class);
            byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(keyPairInfo.getPublicKey());
            log.info("获取RSA公钥成功,randomKey:[{}]", randomKey);
            return publicKeyBytes;
        } catch (Exception e) {
            log.error("公钥Base64解码失败,randomKey:[{}]", randomKey, e);
            throw new BizException("公钥解码失败");
        }
    }

    /**
     * 公钥信息
     */
    @Data
    public static class PublicKeyInfo {
        private String randomKey;
        private String publicKey;

        public PublicKeyInfo() {
        }

        public PublicKeyInfo(String randomKey, String publicKey) {
            this.randomKey = randomKey;
            this.publicKey = publicKey;
        }
    }
}
