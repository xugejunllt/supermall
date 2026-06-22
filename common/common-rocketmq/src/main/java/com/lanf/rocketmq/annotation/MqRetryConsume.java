package com.lanf.rocketmq.annotation;

import com.lanf.rocketmq.sevice.MqRetryStrategy;
import com.lanf.rocketmq.sevice.impl.DefaultMqRetryStrategyImpl;

import java.lang.annotation.*;

/**
 * MQ重试消费注解
 * <p>标记需要幂等控制和失败重试的方法</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqRetryConsume {

    /**
     * 消息ID表达式，支持SpEL，如"#message.id"
     */
    String messageId();

    Class<? extends MqRetryStrategy> retryStrategy() default DefaultMqRetryStrategyImpl.class;
}
