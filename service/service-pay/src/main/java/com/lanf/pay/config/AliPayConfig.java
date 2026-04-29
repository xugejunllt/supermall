package com.lanf.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Data
@Configuration
@ConfigurationProperties(prefix = "pay.ali")

public class AliPayConfig implements Serializable {

    private String notifyUrl;
    private String serverUrl;
    private String appId;
    private String format;
    private String charset;
    private String signType;
    private String appCertPath;
    private String alipayPublicCertPath;
    private String rootCertPath;
    private String privateKey;


}
