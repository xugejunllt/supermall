package com.lanf.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {"com.lanf.pay.mapper","com.lanf.tcc.mapper","com.lanf.rocketmq.mapper"})
@SpringBootApplication(scanBasePackages="com.lanf",exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
//@ImportResource({"classpath:applicationContext.xml"})

@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.finance.api"})
public class PayApplication {
    public static void main(String[] args) {

        SpringApplication.run(PayApplication.class, args);

    }

}
