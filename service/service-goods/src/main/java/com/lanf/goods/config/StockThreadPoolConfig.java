package com.lanf.goods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class StockThreadPoolConfig {

    /**
     * 库存扣减专用线程池
     * - 核心线程数：CPU 核数
     * - 最大线程数：CPU 核数 * 2
     * - 队列：有界队列，防止 OOM
     * - 拒绝策略：CallerRunsPolicy（主线程执行，降级保护）
     */
    @Bean("stockDeductExecutor")
    public ExecutorService stockDeductExecutor() {
        int coreSize = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                coreSize,
                coreSize * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "stock-deduct-" + counter.incrementAndGet());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.prestartAllCoreThreads();
        return executor;
    }
}
