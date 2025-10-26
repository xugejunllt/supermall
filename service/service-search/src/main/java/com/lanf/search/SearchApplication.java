package com.lanf.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient
//nacos注册
public class SearchApplication {
    public static void main(String[] args) {

        SpringApplication.run(SearchApplication.class, args);
    }

}
