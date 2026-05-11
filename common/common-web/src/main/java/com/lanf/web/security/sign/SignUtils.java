package com.lanf.web.security.sign;


import com.lanf.constant.exception.BizException;
import com.lanf.web.security.encrypt.AesEncryptUtils;
import com.lanf.web.security.keygen.RsaEncryptKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AES签名工具类
 */
@Slf4j
@Component
public class SignUtils {

    @Autowired
    private RsaEncryptKeyManager publicKeyManager;

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
                .filter(entry -> entry.getValue() != null && !"sign".equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    /**
     * 验证签名
     * 
     * @param params 参数Map（包含sign字段）
     * @return true-验证通过，false-验证失败
     */
    public boolean verifySign(byte[] aesKeyBytes, Map<String, Object> params) {


        if (params == null || params.isEmpty()) {
            log.warn("签名参数为空");
            return false;
        }

        String sign = (String) params.get("sign");
        if (sign == null || sign.isEmpty()) {
            log.warn("签名为空");
            return false;
        }

        try {

            //2.生成待验签字符串
            String signString = generateSignString(params);
            log.debug("待验签字符串: {}", signString);
            
            //3.使用AES密钥解密签名
            String decryptedData = AesEncryptUtils.decryptByAes(aesKeyBytes, sign);
            
            //4.比较解密后的数据与原始数据是否一致
            boolean valid = signString.equals(decryptedData);
            log.debug("AES解密验签结果: {}, 原始数据长度: {}, 解密数据长度: {}", 
                    valid, signString.length(), decryptedData.length());
            
            return valid;
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }
}
