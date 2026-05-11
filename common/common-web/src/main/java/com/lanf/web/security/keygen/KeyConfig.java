package com.lanf.web.security.keygen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Jarven
 * @date: 2026-02-25 13:47
 * @description:
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "key-gen")
@RefreshScope
 class KeyConfig {

    /**
     * 队列最大密钥数量
     */
    private int maxPoolSize = 5000;
    /**
     * 生成密钥的线程数--用于初始化批量生成密钥任务
     */
    private int threadCount = 2;
    /**
     * 每次生成的密钥数量--用于初始化批量生成密钥任务
     */
    private int batchSize = 1000;

    /**
     * 生成新秘钥的数量-每小时刷新的密钥数
     */
    private int  newKeysGenerated = 100;
    /**
     * 可使用的秘钥数量
     */
    private int numAvailableIntervals = 4000;


}
