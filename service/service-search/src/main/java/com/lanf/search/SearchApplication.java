package com.lanf.search;

import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages="com.lanf")
@MapperScan(basePackages = {"com.lanf.search.mapper", SendMqMessageDO.MAN_SCAN_PACKAGE})
@EnableDiscoveryClient
@EnableScheduling  // 启用定时任务支持
//nacos注册
public class SearchApplication {
    public static void main(String[] args) {

        SpringApplication.run(SearchApplication.class, args);
    }

}
