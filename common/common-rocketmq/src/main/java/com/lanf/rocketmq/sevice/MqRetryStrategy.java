package com.lanf.rocketmq.sevice;

/**
 * MQ重试策略
 * <p>18个重试级别，每个级别不同的延迟策略</p>
 */

public interface MqRetryStrategy {

    long getDelayMillis(int retryCount);

    int maxRetryCount();
}
