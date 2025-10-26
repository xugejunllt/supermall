package com.lanf.storage;

import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {"com.lanf.storage.mapper", SendMqMessageDO.MAN_SCAN_PACKAGE})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api","com.lanf.order.api","com.lanf.goods.api","com.lanf.system.api"})
@EnableHystrix

public class WarehousApplication {
    public static void main(String[] args) {

        SpringApplication.run(WarehousApplication.class, args);
    }

}
