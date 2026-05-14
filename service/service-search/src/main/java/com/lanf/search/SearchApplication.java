package com.lanf.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages="com.lanf")
@MapperScan(basePackages = {"com.lanf.search.mapper"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.lanf.api.order.api"})

//nacos注册
public class SearchApplication {
    public static void main(String[] args) {

        SpringApplication.run(SearchApplication.class, args);
    }

}
