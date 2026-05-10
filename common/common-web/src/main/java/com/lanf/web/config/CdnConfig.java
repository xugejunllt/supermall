package com.lanf.web.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Data
@Configuration
public class CdnConfig {

    private Integer active = 2;

    private String aliyun = "https://alyun";

    private String huaweiyun = " https://huaweiyun";

    private String local = "http://localhost:9013";


}
