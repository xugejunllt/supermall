package com.lanf.dynamicsrrefresh.core.config;


import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.lanf.dynamicsrrefresh.core.store.StoreService;
import com.lanf.dynamicsrrefresh.core.store.impl.NacosStoreServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class StoreConfig {


    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;
    private final static String namespace = "dynamicsrrefresh";

    @Bean
    public StoreService load() {

        // 创建配置服务
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            e.printStackTrace();
            throw new RuntimeException("创建ConfigService对象异常");
        }

        return new NacosStoreServiceImpl(configService);
    }


}
