package com.lanf.web.security.keygen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    @Autowired
    private KeyConfig keyConfig;

    @Bean("keyGenExecutor")
    public Executor keyGenExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(keyConfig.getThreadCount());
        // 最大线程数
        executor.setMaxPoolSize(100);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("key-gen-exec-");
        // 初始化
        executor.initialize();
        return executor;
    }
}