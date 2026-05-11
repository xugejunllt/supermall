package com.lanf.web.security.keygen;


import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.Base64Utils;
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
 * 公钥管理服务类
 */
@Slf4j
@Component
public class PublicKeyManager {

    private static final String PUBLIC_KEY_CACHE_PREFIX = "public:key:%s";
    private static final long CACHE_EXPIRE_TIME = 10;
    private static final TimeUnit CACHE_EXPIRE_UNIT = TimeUnit.MINUTES;

    @Qualifier("encryptKeyManagerServiceImpl")
    @Autowired
    private KeyManagerService keyManagerService;

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 生成公钥信息并缓存密钥对
     * 
     * @return PublicKeyInfo 包含随机key和公钥
     */
    public PublicKeyInfo generatePublicKey() {
        IKeyPairInfo keyPairInfo = keyManagerService.findKeyPairInfo();
        
        String randomKey = UUID.randomUUID().toString().replace("-", "").substring(0, 40);
        
        String cacheKey = String.format( PUBLIC_KEY_CACHE_PREFIX, randomKey);
        String keyPairJson = JsonUtils.toJsonString(keyPairInfo);
        
        redissonCacheService.set(cacheKey, keyPairJson, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        
        log.info("生成公钥成功,randomKey:[{}]", randomKey);
        
        return new PublicKeyInfo(randomKey, keyPairInfo.getPublicKey());
    }

    /**
     * 根据随机key获取私钥（Base64解码后的字节数组）
     * 
     * @param randomKey 随机key
     * @return 私钥字节数组
     */
    public byte[] getPrivateKeyBytes(String randomKey) {
        if (randomKey == null || randomKey.isEmpty()) {
            throw new BizException("随机key不能为空");
        }

        String cacheKey = String.format( PUBLIC_KEY_CACHE_PREFIX, randomKey);
        String keyPairJson = redissonCacheService.get(cacheKey);
        
        if (keyPairJson == null || keyPairJson.isEmpty()) {
            log.warn("密钥对不存在或已过期,randomKey:[{}]", randomKey);
            throw new BizException("密钥对不存在或已过期");
        }
        
        IKeyPairInfo keyPairInfo = JsonUtils.toObject(keyPairJson, IKeyPairInfo.class);
        if (keyPairInfo == null || keyPairInfo.getPrivateKey() == null) {
            log.error("密钥对解析失败,randomKey:[{}]", randomKey);
            throw new BizException("密钥对解析失败");
        }
        
        try {
            byte[] privateKeyBytes = Base64Utils.decodeToBytes(keyPairInfo.getPrivateKey());
            log.info("获取私钥成功,randomKey:[{}]", randomKey);
            return privateKeyBytes;
        } catch (Exception e) {
            log.error("私钥Base64解码失败,randomKey:[{}]", randomKey, e);
            throw new BizException("私钥解码失败");
        }
    }

    /**
     * 根据随机key获取公钥（Base64解码后的字节数组）
     * 
     * @param randomKey 随机key
     * @return 公钥字节数组
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
        
        IKeyPairInfo keyPairInfo = JsonUtils.toObject(keyPairJson, IKeyPairInfo.class);
        if (keyPairInfo == null || keyPairInfo.getPublicKey() == null) {
            log.error("密钥对解析失败,randomKey:[{}]", randomKey);
            throw new BizException("密钥对解析失败");
        }
        
        try {
            byte[] publicKeyBytes = Base64Utils.decodeToBytes(keyPairInfo.getPublicKey());
            log.info("获取公钥成功,randomKey:[{}]", randomKey);
            return publicKeyBytes;
        } catch (Exception e) {
            log.error("公钥Base64解码失败,randomKey:[{}]", randomKey, e);
            throw new BizException("公钥解码失败");
        }
    }

    /**
     * 删除缓存的密钥对
     * 
     * @param randomKey 随机key
     */
    public void removeKeyPair(String randomKey) {
        if (randomKey == null || randomKey.isEmpty()) {
            return;
        }
        
        String cacheKey = String.format("%s%s", PUBLIC_KEY_CACHE_PREFIX, randomKey);
        redissonCacheService.delete(cacheKey);
        log.info("删除密钥对缓存,randomKey:[{}]", randomKey);
    }

    /**
     * 公钥信息内部类
     */
    @Data
    public static class PublicKeyInfo {
        private final String randomKey;
        private final String publicKey;

        public PublicKeyInfo(String randomKey, String publicKey) {
            this.randomKey = randomKey;
            this.publicKey = publicKey;
        }

    }
}
