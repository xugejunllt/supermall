package com.lanf.storage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {"com.lanf.storage.mapper"})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api",
        "com.lanf.api.goods.api","com.lanf.api.order.api"})

public class StorageApplication {
    public static void main(String[] args) {

        SpringApplication.run(StorageApplication.class, args);
    }

}
