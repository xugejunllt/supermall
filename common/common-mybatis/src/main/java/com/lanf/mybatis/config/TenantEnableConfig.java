package com.lanf.mybatis.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
@Getter
@Configuration
public class TenantEnableConfig {

    @Value("${tenant.enable:false}")
    private Boolean enable;


}
