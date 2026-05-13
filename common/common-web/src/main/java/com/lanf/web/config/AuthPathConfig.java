package com.lanf.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
@RefreshScope
public class AuthPathConfig {


    private List<String> excludeAuthPaths;

    private List<String> internalServicePaths;

    /**
     * Admin 路径匹配模式列表
     * 用于识别需要特殊处理的管理后台接口路径
     */
    private List<String> adminPaths;
}
