package com.lanf.logistics;

import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@MapperScan(basePackages = {"com.lanf.logistics.mapper", SendMqMessageDO.MAN_SCAN_PACKAGE})

@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api","com.lanf.order.api"})
public class LogisticsApplication {
    public static void main(String[] args) {

        SpringApplication.run(LogisticsApplication.class, args);
    }

}
