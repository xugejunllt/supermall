package com.lanf.file.service.manager.impl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileConfig {

    private int imageMax;

    private String localPath;

    private String localFileUrlPre;

    private String domain;

}
