package com.lanf.file.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类。
 * 允许所有路径的跨域请求。
 * 配置允许的源地址、请求方法和请求头。
 * 
 * @author mijiupro
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置跨域映射。
     * 
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径生效
                .allowedOrigins("*") // 允许所有源地址
                // .allowedOrigins("https://mijiupro.com","https://mijiu.com ") // 允许的源地址（数组）
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的请求方法
                .allowedHeaders("*"); // 允许的请求头
    }
}