package com.lanf.mybatis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import java.util.List;
 
/**
 * 多租户配置属性类
 *
 * @author hege
 * @Date 2023-08-25
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

    /**
     * 租户id字段名
     */
    private String column = "tenant_id";
 
    /**
     * 需要进行租户id过滤的表名集合
     */
    private List<String> filterTables;
 

 

}