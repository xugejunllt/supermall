package com.lanf.comment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan(basePackages = {"com.lanf.commen.mapper"})
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages="com.lanf",exclude = {
        DataSourceAutoConfiguration.class
})
//nacos注册
public class CommentApplication {
    public static void main(String[] args) {

        SpringApplication.run(CommentApplication.class, args);
    }

}
