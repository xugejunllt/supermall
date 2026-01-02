package com.lanf.order;

import com.lanf.common.utils.BeanUtil;
import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.dromara.hmily.springcloud.feign.HmilyFeignInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportResource;

@MapperScan(basePackages = {"com.lanf.order.mapper", SendMqMessageDO.MAN_SCAN_PACKAGE})

@SpringBootApplication(scanBasePackages="com.lanf",exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableDiscoveryClient  //nacos注册
@ImportResource({"classpath:applicationContext.xml"})
@EnableFeignClients(basePackages = {"com.lanf.log.api","com.lanf.goods.api","com.lanf.pay.api",
        "com.lanf.system.api","com.lanf.logistics.api","com.lanf.welfare.api"})
public class OrderApplication {
    public static void main(String[] args) {

        SpringApplication.run(OrderApplication.class, args);


    }

}
