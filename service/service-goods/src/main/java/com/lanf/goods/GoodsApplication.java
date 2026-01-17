package com.lanf.goods;

import com.lanf.constant.constant.Constants;
import com.lanf.messagemanager.client.model.SendMqMessageDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
@MapperScan(basePackages = {"com.lanf.goods.mapper",
        SendMqMessageDO.MAN_SCAN_PACKAGE, Constants.TCC_MAPPER_PAGE
})
@SpringBootApplication(scanBasePackages="com.lanf",exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableDiscoveryClient  //nacos注册
@EnableFeignClients(basePackages = {"com.lanf.system.api","com.lanf.storage.api",
        "com.lanf.search.api","com.lanf.welfare.api"})
public class GoodsApplication {
    public static void main(String[] args) {

        SpringApplication.run(GoodsApplication.class, args);
    }

}
