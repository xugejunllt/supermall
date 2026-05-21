package com.lanf.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@MapperScan(basePackages = {"com.lanf.finance.mapper"})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api","com.lanf.pay.api","com.lanf.system.api","com.lanf.order.api"})
public class FinanceApplication {
    public static void main(String[] args) {

        SpringApplication.run(FinanceApplication.class, args);
    }

}
