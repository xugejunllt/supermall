package com.lanf.welfare;

import com.lanf.constant.constant.Constants;
import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {"com.lanf.welfare.mapper",
        SendMqMessageDO.MAN_SCAN_PACKAGE, Constants.TCC_MAPPER_PAGE})
@SpringBootApplication(scanBasePackages="com.lanf")
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api","com.lanf.system.api"})
public class WelfareApplication {
    public static void main(String[] args) {

        SpringApplication.run(WelfareApplication.class, args);
    }

}
