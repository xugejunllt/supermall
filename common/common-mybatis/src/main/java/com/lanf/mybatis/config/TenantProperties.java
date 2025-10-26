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
     * 是否开启多租户
     */
    private Boolean enable = true;
 
    /**
     * 租户id字段名
     */
    private String column = "tenant_code";
 
    /**
     * 需要进行租户id过滤的表名集合
     */
    private List<String> filterTables;
 
    /**
     * 需要忽略的多租户的表，此配置优先filterTables，若此配置为空则启用filterTables
     */
    private List<String> ignoreTables;
 

}