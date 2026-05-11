package com.lanf.web.security.encrypt;


import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密工具类
 */
@Slf4j
@Component
public class RsaEncryptUtils {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";



    /**
     * 使用公钥加密数据
     * 
     * @param publicKeyBytes 公钥字节数组
     * @param plainText 明文数据
     * @return 密文（Base64编码）
     */
    public String encryptByPublicKey(byte[] publicKeyBytes, String plainText) {
        if (publicKeyBytes == null || publicKeyBytes.length == 0) {
            throw new BizException("公钥不能为空");
        }
        if (plainText == null || plainText.isEmpty()) {
            throw new BizException("明文数据不能为空");
        }
        
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("公钥加密失败", e);
            throw new BizException("公钥加密失败");
        }
    }

    /**
     * 使用私钥解密数据
     * 
     * @param privateKeyBytes 私钥字节数组
     * @param encryptedText 密文（Base64编码）
     * @return 明文数据
     */
    public String decryptByPrivateKey(byte[] privateKeyBytes, String encryptedText) {
        if (privateKeyBytes == null || privateKeyBytes.length == 0) {
            throw new BizException("私钥不能为空");
        }
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new BizException("密文数据不能为空");
        }
        
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("私钥解密失败", e);
            throw new BizException("私钥解密失败");
        }
    }






}
