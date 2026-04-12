package com.lanf.pay.service.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Data
@Configuration
@ConfigurationProperties(prefix = "pay")
public class PayConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付超时时间（分钟）
     */
    private Integer expireInterval;

}
