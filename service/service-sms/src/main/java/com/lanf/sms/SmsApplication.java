package com.lanf.sms;

import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@MapperScan(basePackages = {"com.lanf.sms.mapper", SendMqMessageDO.MAN_SCAN_PACKAGE})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.lanf.log.api"})
//nacos注册
public class SmsApplication {
    public static void main(String[] args) {

        SpringApplication.run(SmsApplication.class, args);
    }

}
