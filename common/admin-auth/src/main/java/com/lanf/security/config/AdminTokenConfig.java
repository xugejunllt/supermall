package com.lanf.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 管理员 Token 配置类
 * 统一管理 Access Token 和 Refresh Token 的过期时间
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "token.admin")
@RefreshScope
public class AdminTokenConfig {

    /**
     * Access Token 过期时间（分钟）
     * 默认值：10080 分钟（7天）
     */
    private Long accessTokenExpMinutes = 10080L;

    /**
     * Refresh Token 过期时间（分钟）
     * 默认值：43200 分钟（30天）
     */
    private Long refreshTokenExpMinutes = 43200L;
}
