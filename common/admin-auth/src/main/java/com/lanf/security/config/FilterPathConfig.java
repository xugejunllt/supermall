package com.lanf.security.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Data
@Configuration
@ConfigurationProperties(prefix = "filter")
@RefreshScope
public class FilterPathConfig {

    private List<String> adminNotTokenPath;

    private List<String> userNotTokenPath;

    private List<String> autoIgnoreTenantPath;
}
