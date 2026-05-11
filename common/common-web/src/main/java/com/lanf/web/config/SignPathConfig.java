package com.lanf.web.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 签名验证配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "sign")
@RefreshScope
public class SignPathConfig {

    /**
     * 是否启用签名验证
     */
    private boolean enabled = true;

    /**
     * 需要签名验证的路径列表（支持Ant风格匹配）
     */
    private List<String> paths = new ArrayList<>();
}
