package com.lanf.seckill.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class SeckillThreadPoolConfig {

    /**
     * 秒杀业务专用线程池
     * 用于并行查询 Redis 等 IO 密集型操作
     */
    @Bean("seckillQueryExecutor")
    public ThreadPoolTaskExecutor seckillQueryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：根据 CPU 核数和 IO 等待时间调整
        executor.setCorePoolSize(10);
        
        // 最大线程数
        executor.setMaxPoolSize(20);
        
        // 队列容量
        executor.setQueueCapacity(200);
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀
        executor.setThreadNamePrefix("seckill-query-");
        
        // 拒绝策略：由调用线程执行，保证不丢失任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("秒杀查询线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
