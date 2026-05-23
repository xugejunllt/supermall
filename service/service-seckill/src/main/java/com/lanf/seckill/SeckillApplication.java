package com.lanf.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@MapperScan(basePackages = {"com.lanf.seckill.mapper"})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.lanf.api.goods"})
//nacos注册
public class SeckillApplication {

    public static void main(String[] args) {

        SpringApplication.run(SeckillApplication.class, args);
    }

}
