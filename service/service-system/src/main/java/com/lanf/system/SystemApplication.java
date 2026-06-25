package com.lanf.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {"com.lanf.system.mapper","com.lanf.rocketmq.mapper"})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.api.goods.api","com.lanf.api.storage.api",
        "com.lanf.api.search","com.lanf.welfare.api","com.lanf.api.order.api"
,"com.lanf.api.pay.api"})
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);

    }


}
