package com.lanf.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String urls;
    
    @Value("${spring.redis.port}")
    private String port;
    
    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${redisson.connection-pool-size:64}")
    private int connectionPoolSize;

    @Value("${redisson.connection-minimum-idle-size:24}")
    private int connectionMinimumIdleSize;

    @Value("${redisson.idle-connection-timeout:10000}")
    private int idleConnectionTimeout;

    @Value("${redisson.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${redisson.timeout:3000}")
    private int timeout;

    @Value("${redisson.retry-attempts:3}")
    private int retryAttempts;

    @Value("${redisson.retry-interval:1500}")
    private int retryInterval;

    @Value("${redisson.lock-watchdog-timeout:30000}")
    private long lockWatchdogTimeout;

    @Bean(name = "redissonClient")
    public RedissonClient redissonClientSingle() {
        
        Config config = new Config();
        
        SingleServerConfig singleServerConfig = config.useSingleServer();
        
        singleServerConfig.setAddress("redis://" + urls + ":" + port);
        
        singleServerConfig.setDatabase(database);
        
        if (!StringUtils.isEmpty(password)) {
            singleServerConfig.setPassword(password);
        }
        
        singleServerConfig.setConnectionPoolSize(connectionPoolSize);
        
        singleServerConfig.setConnectionMinimumIdleSize(connectionMinimumIdleSize);
        
        singleServerConfig.setIdleConnectionTimeout(idleConnectionTimeout);
        
        singleServerConfig.setConnectTimeout(connectTimeout);
        
        singleServerConfig.setTimeout(timeout);
        
        singleServerConfig.setRetryAttempts(retryAttempts);
        
        singleServerConfig.setRetryInterval(retryInterval);
        
        config.setLockWatchdogTimeout(lockWatchdogTimeout);
        
        log.info("Redisson配置初始化完成 - 地址:{}:{}, 连接池大小:{}, 最小空闲连接:{}, 看门狗超时:{}ms",
                urls, port, connectionPoolSize, connectionMinimumIdleSize, lockWatchdogTimeout);

        return Redisson.create(config);
    }
}