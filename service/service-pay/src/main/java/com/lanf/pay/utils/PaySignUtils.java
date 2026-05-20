package com.lanf.pay.utils;


import com.lanf.constant.exception.BizException;
import com.lanf.pay.config.PayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component
public class PaySignUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    @Autowired
    private PayConfig payConfig;

    private static String secretKey;

    @PostConstruct
    public void init() {
        secretKey = payConfig.getSignSecretKey();
    }

    /**
     * 使用 HMAC-SHA256 生成签名
     *
     * @param data 待签名的数据
     * @return Base64 编码的签名值
     */
    public static String generateHmacSha256Sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("HMAC-SHA256 签名失败");
            throw new BizException("生成签名失败");
        }
    }

    /**
     * 验证签名
     *
     * @param data 原始数据
     * @param sign 待验证的签名
     * @return 是否验证通过
     */
    public static boolean verifySign(String data, String sign) {
        String generatedSign = generateHmacSha256Sign(data);
        boolean result = generatedSign.equals(sign);
        if (!result) {
            log.error("签名失败,data[{}],generatedSign[{}],sign[{}]", data, generatedSign, sign);
        }
        return result;
    }
}
