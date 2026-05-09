package com.lanf.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 搜索业务专用线程池配置
 */
@Configuration
public class SearchThreadPoolConfig {


    @Bean("searchTaskExecutor")
    public ThreadPoolTaskExecutor searchTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);
        
        // 最大线程数：CPU核数 * 4
        executor.setMaxPoolSize(40);
        
        // 队列容量：缓冲突发流量
        executor.setQueueCapacity(200);
        
        // 线程空闲时间：60秒
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀：方便日志排查
        executor.setThreadNamePrefix("search-pool-");
        
        // 拒绝策略：由调用线程处理（保证搜索请求不丢失，但会增加响应时间）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
