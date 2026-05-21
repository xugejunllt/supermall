package com.lanf.rocketmq.util;

import com.lanf.constant.mq.base.BaseMessage;
import com.lanf.constant.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 消费者链路追踪切面
 * <p>通过 AOP 拦截所有 RocketMQ 消费者的 onMessage 方法，自动设置和清理 traceId</p>
 *
 * @author system
 * @since 2024-01-15
 */
@Slf4j
@Aspect
@Component
public class RocketMqTraceAspect {


    /**
     * 环绕通知：拦截所有 RocketMQ 消费者的 onMessage 方法
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("execution(* org.apache.rocketmq.spring.core.RocketMQListener.onMessage(..))")
    public Object traceAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("RocketMQ 监听到消息, traceId: {}", TraceIdUtils.getTraceId());
        Object[] args = joinPoint.getArgs();
        String traceId = null;

        try {
            // 1. 从方法参数中提取 traceId
            if (args != null && args.length > 0) {
                Object arg = args[0];
                if (arg instanceof BaseMessage) {
                    traceId = ((BaseMessage) arg).getTraceId();
                }
            }
            TraceIdUtils.setTraceId(traceId);

            return joinPoint.proceed();

        } catch (Throwable e) {
            log.error("RocketMQ 消费消息异常, traceId: {}", traceId, e);
            throw e;
        } finally {
            TraceIdUtils.clearAll();
        }
    }
}
