package com.lanf.seckill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "seckill-coupon")
@RefreshScope //
@Data
public class SeckillCouponUrlConfig {

    private List<UrlMapping> urlMappings = new ArrayList<>();
    // getter / setter
    @Data
    public static class UrlMapping {

        private String path;

        private Long seckillItemId;

    }
}