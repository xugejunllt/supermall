package com.lanf.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AES密钥配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "aes")
public class AesKeyConfig {

    /**
     * AES密钥（Base64编码）
     */
    private String key;

}
