package com.lanf.web.security.keygen;


import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.web.constant.WebRedisKeyConstants;
import com.lanf.web.security.encrypt.RsaEncryptUtils;
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

    private static final long CACHE_EXPIRE_TIME = 10;

    private static final TimeUnit CACHE_EXPIRE_UNIT = TimeUnit.MINUTES;

    @Autowired
    @Qualifier("encryptKeyManagerServiceImpl")
    private AbstractKeyManagerService keyManagerService;

    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private RsaEncryptUtils rsaEncryptUtils;

    /**
     * 生成RSA密钥对并缓存
     * 
     * @return PublicKeyInfo 包含随机key和RSA公钥（Base64编码）
     */
    public PublicKeyInfo generatePublicKey() {
        IKeyPairInfo keyPairInfo = keyManagerService.findKeyPairInfo();
        
        String randomKey = UUID.randomUUID().toString().replace("-", "");
        
        String cacheKey = String.format(WebRedisKeyConstants.RSA_KEY_PAIR_CACHE, randomKey);
        String keyPairJson = JsonUtils.toJsonString(keyPairInfo);
        
        redissonCacheService.set(cacheKey, keyPairJson, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        
        log.info("生成RSA密钥对成功,randomKey:[{}]", randomKey);
        
        return new PublicKeyInfo(randomKey, keyPairInfo.getPublicKey());
    }

    /**
     * 通过randomKey获取私钥，对传入的加密数据进行解密，并验证数据一致性
     * 
     * @param randomKey 随机key
     * @param encryptedData Base64编码的加密数据
     * @return 解密后的明文数据
     * @throws BizException 解密失败或数据不一致时抛出异常
     */
    public String decryptAndVerify(String randomKey, String encryptedData) {
        //1.参数校验
        if (randomKey == null || randomKey.isEmpty()) {
            throw new BizException("randomKey不能为空");
        }
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new BizException("加密数据不能为空");
        }


        //2.从Redis获取密钥对JSON
        String cacheKey = String.format(WebRedisKeyConstants.RSA_KEY_PAIR_CACHE, randomKey);
        String keyPairJson = redissonCacheService.get(cacheKey);
        
        if (keyPairJson == null || keyPairJson.isEmpty()) {
            log.warn("Redis中未找到密钥对, randomKey: {}", randomKey);
            throw new BizException("密钥已过期或不存在");
        }

        //3.解析密钥对JSON
        IKeyPairInfo keyPairInfo = JsonUtils.toObject(keyPairJson, IKeyPairInfo.class);
        if (keyPairInfo == null || keyPairInfo.getPrivateKey() == null) {
            log.error("密钥对解析失败, randomKey: {}", randomKey);
            throw new BizException("密钥对解析失败");
        }

        //4.Base64解码私钥
        byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(keyPairInfo.getPrivateKey());

        //5.使用私钥解密数据
        String decryptedData = rsaEncryptUtils.decryptByPrivateKey(privateKeyBytes, encryptedData);
        log.debug("解密成功, randomKey: {}", randomKey);

        log.info("解密并验证成功, randomKey: {}", randomKey);
        return decryptedData;
    }

    /**
     * 通过randomKey获取公钥，对传入的数据进行加密
     * 
     * @param randomKey 随机key
     * @param plainText 明文数据
     * @return Base64编码的加密数据
     * @throws BizException 加密失败时抛出异常
     */
    public String encryptByRandomKey(String randomKey, String plainText) {
        //1.参数校验
        if (randomKey == null || randomKey.isEmpty()) {
            throw new BizException("randomKey不能为空");
        }
        if (plainText == null || plainText.isEmpty()) {
            throw new BizException("明文数据不能为空");
        }

        //2.从Redis获取密钥对JSON
        String cacheKey = String.format(WebRedisKeyConstants.RSA_KEY_PAIR_CACHE, randomKey);
        String keyPairJson = redissonCacheService.get(cacheKey);
        
        if (keyPairJson == null || keyPairJson.isEmpty()) {
            log.warn("Redis中未找到密钥对, randomKey: {}", randomKey);
            throw new BizException("密钥已过期或不存在");
        }

        //3.解析密钥对JSON
        IKeyPairInfo keyPairInfo = JsonUtils.toObject(keyPairJson, IKeyPairInfo.class);
        if (keyPairInfo == null || keyPairInfo.getPublicKey() == null) {
            log.error("密钥对解析失败, randomKey: {}", randomKey);
            throw new BizException("密钥对解析失败");
        }

        //4.Base64解码公钥
        byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(keyPairInfo.getPublicKey());

        //5.使用公钥加密数据
        String encryptedData = rsaEncryptUtils.encryptByPublicKey(publicKeyBytes, plainText);
        log.debug("加密成功, randomKey: {}", randomKey);

        return encryptedData;
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
