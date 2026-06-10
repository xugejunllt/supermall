package com.lanf.web.security.sign;


import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HMAC-SHA256签名工具类
 */
@Slf4j
@Component
public class SignUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

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
     * 使用HMAC-SHA256生成签名
     *
     * @param keyBytes    对称签名密钥（字节数组）
     * @param signString 待签名字符串
     * @return Base64编码的签名值
     */
    public String generateSign(byte[] keyBytes, String signString) {
        if (keyBytes == null || keyBytes.length == 0) {
            throw new BizException("签名密钥不能为空");
        }
        if (signString == null || signString.isEmpty()) {
            throw new BizException("待签名字符串不能为空");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(signString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("HMAC-SHA256签名生成失败", e);
            throw new BizException("签名生成失败");
        }
    }

    /**
     * 验证签名
     *
     * @param keyBytes 对称签名密钥（字节数组）
     * @param params   参数Map（包含sign字段）
     * @return true-验证通过，false-验证失败
     */
    public boolean verifySign(byte[] keyBytes, Map<String, Object> params) {

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
            // 1. 生成待验签字符串
            String signString = generateSignString(params);
            log.debug("待验签字符串: {}", signString);

            // 2. 使用HMAC-SHA256重新计算签名
            String expectedSign = generateSign(keyBytes, signString);

            // 3. 比较计算出的签名与传入的签名是否一致
            boolean valid = sign.equals(expectedSign);
            log.debug("HMAC-SHA256验签结果: {}, 传入签名: {}, 期望签名: {}",
                    valid, sign, expectedSign);

            return valid;
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }
}
