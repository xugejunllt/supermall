package com.lanf.user;

import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {"com.lanf.user.mapper",SendMqMessageDO.MAN_SCAN_PACKAGE})
@SpringBootApplication(scanBasePackages = "com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api"})
public class UserApplication {
    public static void main(String[] args) {

        SpringApplication.run(UserApplication.class, args);
    }

}
