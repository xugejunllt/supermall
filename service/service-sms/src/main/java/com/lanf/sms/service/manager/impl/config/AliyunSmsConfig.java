package com.lanf.sms.service.manager.impl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sms.aliyun")
@Data
public class AliyunSmsConfig {

    private boolean active;

    private String signName;

    private String accessKeyId;

    private String accessKeySecret;

    private String regionId;

    private String product;

    private String domain;
}
