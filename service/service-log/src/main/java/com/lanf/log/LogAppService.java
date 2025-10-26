package com.lanf.log;

import com.lanf.log.aspect.LogAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/4/30 21:43
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.lanf"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {LogAspect.class})})
@MapperScan("com.lanf.log.mapper")
@EnableDiscoveryClient  //nacos注册
public class LogAppService {
    public static void main(String[] args) {
        SpringApplication.run(LogAppService.class, args);
    }
}
