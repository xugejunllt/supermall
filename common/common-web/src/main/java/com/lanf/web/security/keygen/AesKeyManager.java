package com.lanf.web.security.keygen;

import com.lanf.constant.exception.BizException;
import com.lanf.web.config.AesKeyConfig;
import com.lanf.web.security.encrypt.AesEncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * AES密钥管理器
 * 从配置文件读取密钥，提供加解密功能
 */
@Slf4j
@Component
public class AesKeyManager {

    @Autowired
    private AesKeyConfig aesKeyConfig;

    /**
     * AES密钥字节数组
     */
    private byte[] aesKeyBytes;

    /**
     * 初始化：从配置文件读取并解码密钥
     */
    @PostConstruct
    public void init() {


        String keyBase64 = aesKeyConfig.getKey();
        if (keyBase64 == null || keyBase64.isEmpty()) {
            log.error("AES密钥未配置");
            throw new BizException("AES密钥未配置");
        }

        try {
            // Base64解码密钥
            aesKeyBytes = java.util.Base64.getDecoder().decode(keyBase64);
            
            // 验证密钥长度（AES要求16/24/32字节）
            if (aesKeyBytes.length != 16 && aesKeyBytes.length != 24 && aesKeyBytes.length != 32) {
                log.error("AES密钥长度不正确，期望16/24/32字节，实际: {} 字节", aesKeyBytes.length);
                throw new BizException("AES密钥长度不正确");
            }

            log.info("AES密钥加载成功，长度: {} 字节", aesKeyBytes.length);

        } catch (IllegalArgumentException e) {
            log.error("AES密钥Base64解码失败", e);
            throw new BizException("AES密钥格式错误");
        }
    }

    /**
     * 使用配置的AES密钥加密数据
     * 
     * @param plainText 明文数据
     * @return Base64编码的密文
     * @throws BizException 加密失败时抛出异常
     */
    public String encrypt(String plainText) {

        if (plainText == null || plainText.isEmpty()) {
            throw new BizException("明文数据不能为空");
        }

        try {
            String encryptedData = AesEncryptUtils.encryptByAes(aesKeyBytes, plainText);
            log.debug("AES加密成功");
            return encryptedData;
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new BizException("AES加密失败");
        }
    }

    /**
     * 使用配置的AES密钥解密数据
     * 
     * @param encryptedData Base64编码的密文
     * @return 明文数据
     * @throws BizException 解密失败时抛出异常
     */
    public String decrypt(String encryptedData) {


        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new BizException("密文数据不能为空");
        }

        try {
            String decryptedData = AesEncryptUtils.decryptByAes(aesKeyBytes, encryptedData);
            log.debug("AES解密成功");
            return decryptedData;
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new BizException("AES解密失败");
        }
    }




}
