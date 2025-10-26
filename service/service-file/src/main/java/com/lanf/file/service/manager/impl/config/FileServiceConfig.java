package com.lanf.file.service.manager.impl.config;

import com.lanf.file.service.manager.impl.AliyunFileServiceImpl;
import com.lanf.file.service.manager.impl.LocalFileServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileServiceConfig {

    /**
     * 激活阿里云oss
     */
    @Bean
    @ConditionalOnProperty(name = "file.active", havingValue = "0")

    public AliyunFileServiceImpl aliyunFileServiceImpl() {

        return new AliyunFileServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "file.active", havingValue = "2")
    public LocalFileServiceImpl localFileServiceImpl() {

        return new LocalFileServiceImpl();
    }

}
