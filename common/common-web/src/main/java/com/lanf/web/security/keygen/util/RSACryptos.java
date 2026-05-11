package com.lanf.web.security.keygen.util;


import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * RSA加密工具类
 */
@Slf4j
public class RSACryptos {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    /**
     * 生成RSA密钥对
     * 
     * @return KeyPair 密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成RSA密钥对失败", e);
            throw new BizException("生成RSA密钥对失败");
        }
    }
}
