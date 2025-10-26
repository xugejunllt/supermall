package com.lanf.file.service.manager.impl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileConfig fileConfig;

    /**
     * 资源映射:把请求的/file/images/** 映射到该文件根路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler(fileConfig.getLocalFileUrlPre()+"/**").
                addResourceLocations("file:" + fileConfig.getLocalPath());
    }



}
