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
}
