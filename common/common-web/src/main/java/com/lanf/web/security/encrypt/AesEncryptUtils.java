package com.lanf.web.security.encrypt;


import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加解密工具类
 */
@Slf4j
public class AesEncryptUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 使用AES密钥加密数据
     * 
     * @param aesKeyBytes AES密钥字节数组（16/24/32字节）
     * @param plainText 明文数据
     * @return 密文（Base64编码）
     */
    public static String encryptByAes(byte[] aesKeyBytes, String plainText) {
        //1.校验参数是否为空
        if (aesKeyBytes == null || aesKeyBytes.length == 0) {
            throw new BizException("AES密钥不能为空");
        }
        if (plainText == null || plainText.isEmpty()) {
            throw new BizException("明文数据不能为空");
        }

        try {
            //2.创建AES密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, ALGORITHM);
            
            //3.初始化Cipher为加密模式
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            //4.执行AES加密
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            //5.Base64编码并返回
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new BizException("AES加密失败");
        }
    }

    /**
     * 使用AES密钥解密数据
     * 
     * @param aesKeyBytes AES密钥字节数组（16/24/32字节）
     * @param encryptedText 密文（Base64编码）
     * @return 明文数据
     */
    public static String decryptByAes(byte[] aesKeyBytes, String encryptedText) {
        //1.校验参数是否为空
        if (aesKeyBytes == null || aesKeyBytes.length == 0) {
            throw new BizException("AES密钥不能为空");
        }
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new BizException("密文数据不能为空");
        }

        try {
            //2.创建AES密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(aesKeyBytes, ALGORITHM);
            
            //3.初始化Cipher为解密模式
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            //4.Base64解码密文
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            
            //5.执行AES解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            //6.返回明文数据
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new BizException("AES解密失败");
        }
    }

    /**
     * 生成随机AES密钥（16字节=128位）
     * 
     * @return AES密钥字节数组
     */
    public static byte[] generateAesKey() {
        try {
            java.security.SecureRandom secureRandom = java.security.SecureRandom.getInstanceStrong();
            byte[] key = new byte[16];
            secureRandom.nextBytes(key);
            return key;
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new BizException("生成AES密钥失败");
        }
    }
}
