package com.lanf.web.security.keygen.impl;

import com.lanf.web.security.keygen.AbstractKeyManagerService;
import com.lanf.web.security.keygen.model.IKeyPairInfo;
import com.lanf.web.security.keygen.util.RSACryptos;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.security.KeyPair;

/**
 * @author: Jarven
 * @date: 2026-02-28 15:59
 * @description:
 */

@Component
 class EncryptKeyManagerServiceImpl extends AbstractKeyManagerService {

    private final static String SECURITY_QUEUE_TASK_KEY_GEN = "security:queue:task:key_gen";



    @Override
    protected String getRedisQueueKey() {


        return SECURITY_QUEUE_TASK_KEY_GEN;
    }


    @Override
    protected IKeyPairInfo generateKeyPair() {

        // RSA 密钥对是二进制数据
        KeyPair keyPair = RSACryptos.generateKeyPair();

        // 公钥/私钥的二进制字节数组
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

        // 转换为 Base64 字符串，方便存储和传输
        String publicKey = Base64Utils.encodeToString(publicKeyBytes);
        String privateKey = Base64Utils.encodeToString(privateKeyBytes);

        IKeyPairInfo keyPairInfo = new IKeyPairInfo();
        keyPairInfo.setPrivateKey(privateKey);
        keyPairInfo.setPublicKey(publicKey);

        return keyPairInfo;
    }
}
