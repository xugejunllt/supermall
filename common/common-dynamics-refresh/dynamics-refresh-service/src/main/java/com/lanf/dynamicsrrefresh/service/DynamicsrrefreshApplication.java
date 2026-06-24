package com.lanf.dynamicsrrefresh.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//@EnableFeignClients(basePackages = {"com.lanf.dynamicsrrefresh.service.fegin"})
@EnableDiscoveryClient  //nacos注册
@SpringBootApplication(scanBasePackages="com.lanf")
public class DynamicsrrefreshApplication {
    public static void main(String[] args) {

        SpringApplication.run(DynamicsrrefreshApplication.class, args);
    }

}
