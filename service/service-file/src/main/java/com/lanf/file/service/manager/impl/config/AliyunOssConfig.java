package com.lanf.file.service.manager.impl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "oss.aliyun")
public class AliyunOssConfig {

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * endpoint
     */
    private String endpoint;

    /**
     * bucketName
     */
    private String bucketName;
}
