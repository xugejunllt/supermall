package com.lanf.web.security.sign;


import com.lanf.constant.exception.BizException;
import com.lanf.web.security.keygen.PublicKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RSA签名工具类
 */
@Slf4j
@Component
public class SignUtils {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_FIELD_NAME = "sign";

    @Autowired
    private PublicKeyManager publicKeyManager;

    /**
     * 对参数进行排序并生成签名字符串
     * 
     * @param params 参数Map
     * @return 排序后的签名字符串 key1=value1&key2=value2
     */
    public String generateSignString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            throw new BizException("签名参数不能为空");
        }

        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !SIGN_FIELD_NAME.equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    /**
     * 验证签名
     * 
     * @param randomKey 随机key
     * @param params 参数Map（包含sign字段）
     * @return true-验证通过，false-验证失败
     */
    public boolean verifySign(String randomKey, Map<String, Object> params) {
        if (randomKey == null || randomKey.isEmpty()) {
            log.warn("随机key为空");
            return false;
        }

        if (params == null || params.isEmpty()) {
            log.warn("签名参数为空");
            return false;
        }

        String sign = (String) params.get(SIGN_FIELD_NAME);
        if (sign == null || sign.isEmpty()) {
            log.warn("签名为空");
            return false;
        }

        try {
            byte[] publicKeyBytes = publicKeyManager.getPublicKeyBytes(randomKey);
            
            String signString = generateSignString(params);
            log.debug("待验签字符串: {}", signString);
            
            return verifyByPublicKey(publicKeyBytes, signString, sign);
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 使用公钥验证签名
     * 
     * @param publicKeyBytes 公钥字节数组
     * @param data 原始数据
     * @param sign 签名值（Base64编码）
     * @return true-验证通过，false-验证失败
     */
    private boolean verifyByPublicKey(byte[] publicKeyBytes, String data, String sign) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            
            byte[] signBytes = Base64.getDecoder().decode(sign);
            byte[] decryptedBytes = cipher.doFinal(signBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            return data.equals(decryptedData);
        } catch (Exception e) {
            log.error("公钥验签失败", e);
            return false;
        }
    }
}
