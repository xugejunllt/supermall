package com.lanf.rocketmq.annotation;

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

    /**
     * 最大重试次数，默认18次
     */
    int maxRetryCount() default 18;
}
