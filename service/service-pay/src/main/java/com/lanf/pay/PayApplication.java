package com.lanf.pay;

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

@MapperScan(basePackages = {"com.lanf.pay.mapper", SendMqMessageDO.MAN_SCAN_PACKAGE})
@SpringBootApplication(scanBasePackages="com.lanf",exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
//@ImportResource({"classpath:applicationContext.xml"})

@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.log.api","com.lanf.finance.api","com.lanf.welfare.api"})
public class PayApplication {
    public static void main(String[] args) {

        SpringApplication.run(PayApplication.class, args);
        HmilyFeignInterceptor bean = BeanUtil.getBean(HmilyFeignInterceptor.class);

        System.out.println("获取的bean是"+ bean);
    }

}
