package com.lanf.rocketmq.aspect;

import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.sevice.IMqConsumeMessageService;
import com.lanf.rocketmq.sevice.MqRetryReflectExecutor;
import com.lanf.rocketmq.sevice.MqRetryStrategy;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MQ重试消费AOP切面
 * <p>扫描 @MqRetryConsume 注解，实现幂等控制、状态管理和失败重试</p>
 */
@Slf4j
@Aspect
@Component
public class MqRetryConsumeAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private IMqConsumeMessageService mqLocalTransactionMessageService;


    @Autowired
    private MqRetryReflectExecutor mqRetryReflectExecutor;

    @Autowired
    @Qualifier("mqConsumeRetrySendExecutor")
    private Executor mqRetrySendExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    /**
     * 最大重试任务数（队列中同时存在的最大任务数）
     */
    private static final int MAX_RETRY_TASK_COUNT = 10000;

    /**
     * 当前重试任务数统计
     */
    private final AtomicInteger retryTaskCount = new AtomicInteger(0);

    /**
     * 去重容器：已加入重试队列的消息ID（MqConsumeMessageDO.id）
     */
    private final Set<String> retryMessageIdSet = ConcurrentHashMap.newKeySet();

    /**
     * SpEL表达式前缀
     */
    private static final String SPEL_PREFIX = "#";

    /**
     * 分布式锁前缀
     */
    private static final String LOCK_PREFIX = "mq:consume:";

    @Around("@annotation(mqRetryConsume)")
    public Object around(ProceedingJoinPoint joinPoint, MqRetryConsume mqRetryConsume) throws Throwable {
        // 解析消息ID
        String messageId = parseMessageId(joinPoint, mqRetryConsume.messageId());
        if (messageId == null || messageId.isEmpty()) {
            log.error("消息ID解析失败，跳过消费");
            return null;
        }

        String lockKey = LOCK_PREFIX + messageId;
        RLock lock = redissonClient.getLock(lockKey);

        // 1. 分布式锁，获取锁失败直接return
        if (!lock.tryLock()) {
            log.warn("获取分布式锁失败，跳过消费，messageId:{}", messageId);
            return null;
        }

        try {
            // 查询或创建消费记录
            MqConsumeMessageDO messageDO = mqLocalTransactionMessageService.getByMessageId(messageId);
            if (messageDO == null) {
                // 2. 插入消息--正在消费中
                messageDO = createMessageRecord(joinPoint, mqRetryConsume, messageId);
                mqLocalTransactionMessageService.save(messageDO);
            } else if (messageDO.getStatus() != null && messageDO.getStatus() == 1) {
                log.info("消息已消费成功，跳过，messageId:{}", messageId);
                return null;
            }

            // 3. 执行目标方法
            try {
                Object result = joinPoint.proceed();

                // 4. 执行完成，更新消息状态为已完成
                messageDO.setStatus(1);
                messageDO.setErrorMsg(null);
                mqLocalTransactionMessageService.updateById(messageDO);

                log.info("MQ消息消费成功，messageId:{}", messageId);
                return result;
            } catch (Exception e) {
                log.error("MQ消息消费失败，准备延迟重试，messageId:{}", messageId, e);

                // 更新状态为失败
                messageDO.setStatus(2);
                messageDO.setErrorMsg(e.getMessage());
                mqLocalTransactionMessageService.updateById(messageDO);
                if (isRetryException(e)) {

                    sendToRetryQueue(messageDO);
                } else {
                    log.error("【钉钉告警】MQ消息消费超过最大重试次数，messageId:{}",
                            messageDO.getMessageId());
                }
                return null;
            }
        } finally {

            lock.unlock();
        }
    }

    /**
     * 解析消息ID（支持SpEL表达式）
     *
     * @param joinPoint           连接点
     * @param messageIdExpression 消息ID表达式
     * @return 消息ID
     */
    private String parseMessageId(ProceedingJoinPoint joinPoint, String messageIdExpression) {
        if (messageIdExpression == null || messageIdExpression.isEmpty()) {
            return null;
        }

        // 如果不是SpEL表达式，直接返回
        if (!messageIdExpression.startsWith(SPEL_PREFIX)) {
            return messageIdExpression;
        }

        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();

            StandardEvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            return spelExpressionParser.parseExpression(messageIdExpression).getValue(context, String.class);
        } catch (Exception e) {
            log.error("解析消息ID失败，expression:{}", messageIdExpression, e);
            return null;
        }
    }

    /**
     * 创建消息消费记录
     *
     * @param joinPoint      连接点
     * @param mqRetryConsume 注解
     * @param messageId      消息ID
     * @return 消息记录
     */
    private MqConsumeMessageDO createMessageRecord(ProceedingJoinPoint joinPoint, MqRetryConsume mqRetryConsume, String messageId) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Class<? extends MqRetryStrategy> aClass = mqRetryConsume.retryStrategy();
        MqRetryStrategy retryStrategy = applicationContext.getBean(aClass);

        MqConsumeMessageDO messageDO = new MqConsumeMessageDO();
        messageDO.setMessageId(messageId);
        messageDO.setStatus(0);
        messageDO.setRetryCount(0);
        messageDO.setMaxRetryCount(retryStrategy.maxRetryCount());
        messageDO.setClassName(method.getDeclaringClass().getName());
        messageDO.setMethodName(method.getName());
        messageDO.setRetryStrategyBeanClass(aClass.getName());
        // 记录参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] paramTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypeNames[i] = parameterTypes[i].getName();
        }
        messageDO.setParamTypes(JsonUtils.toJsonString(paramTypeNames));

        // 记录参数值
        Object[] args = joinPoint.getArgs();
        messageDO.setParamValues(JsonUtils.toJsonString(args));

        return messageDO;
    }

    /**
     * 发送到重试队列（HashedWheelTimer 延迟执行）
     *
     * @param messageDO 消息记录
     */
    private void sendToRetryQueue(MqConsumeMessageDO messageDO) {

        String messageId = messageDO.getMessageId();

        // 1. 去重检查
        if (!retryMessageIdSet.add(messageId)) {
            log.warn("消息已在重试队列中，跳过重复添加，mqConsumeMessageId:{}", messageId);
            return;
        }

        // 2. 任务数上限检查
        int currentCount = retryTaskCount.incrementAndGet();
        if (currentCount > MAX_RETRY_TASK_COUNT) {
            retryTaskCount.decrementAndGet();
            retryMessageIdSet.remove(messageId);
            log.warn("重试任务数已达上限{}/{}，跳过添加，mqConsumeMessageId:{}", currentCount, MAX_RETRY_TASK_COUNT, messageId);
            return;
        }

        String retryStrategyBeanClass = messageDO.getRetryStrategyBeanClass();
        Class<?> aClass = null;
        try {
            aClass = Class.forName(retryStrategyBeanClass);
        } catch (ClassNotFoundException e) {
            log.error("【钉钉告警】MQ消息消费超过最大重试次数，messageId:{}",
                    messageDO.getMessageId());
            return;
        }

        MqRetryStrategy mqRetryStrategy = (MqRetryStrategy) applicationContext.getBean(aClass);
        int retryCount = messageDO.getRetryCount() + 1;
        if (retryCount > mqRetryStrategy.maxRetryCount()) {
            log.error("【钉钉告警】MQ消息消费超过最大重试次数，messageId:{}, retryCount:{}",
                    messageDO.getMessageId(), retryCount);
            // TODO: 调用钉钉Webhook API 发送告警
            return;
        }

        long delayMillis = mqRetryStrategy.getDelayMillis(retryCount);

        log.info("消息加入重试队列，messageId:{}, mqConsumeMessageId:{}, retryCount:{}, delay:{}ms",
                messageDO.getMessageId(), messageId, retryCount, delayMillis);
        timer.newTimeout(timeout -> {
            mqRetrySendExecutor.execute(() -> doRetry(messageDO, retryCount, mqRetryStrategy));
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重试
     *
     * @param messageDO  消息记录
     * @param retryCount 重试次数
     */
    private void doRetry(MqConsumeMessageDO messageDO, int retryCount, MqRetryStrategy mqRetryStrategy) {
        // 以 messageId 作为分布式锁key，防止同一消息并发重试
        String messageId = messageDO.getMessageId();
        String lockKey = LOCK_PREFIX + messageId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (!locked) {
                log.warn("获取分布式锁失败，跳过本次重试，messageId:{}", messageDO.getMessageId());
                return;
            }
            messageDO = mqLocalTransactionMessageService.getByMessageId(messageDO.getMessageId());
            if (messageDO.getRetryCount() > mqRetryStrategy.maxRetryCount()){
               log.error("钉钉告警");
               return;
            }

            // 更新重试次数
            messageDO.setRetryCount(retryCount);
            mqLocalTransactionMessageService.updateById(messageDO);
            // 通过反射重新执行方法
            mqRetryReflectExecutor.execute(messageDO);
            // 反射执行成功，更新状态
            messageDO.setStatus(1);
            messageDO.setErrorMsg(null);
            mqLocalTransactionMessageService.updateById(messageDO);

            log.info("MQ消息重试消费成功，messageId:{}, , retryCount:{}",
                    messageDO.getMessageId(),  retryCount);
        } catch (Exception e) {
            log.error("MQ消息第{}次重试失败，messageId:{}, mqConsumeMessageId:{}", retryCount, messageDO.getMessageId(), mqConsumeMessageId, e);
            /**
             * 第一次执行时 如果发生业务异常 那么不会进入重试队列
             * 所以重试 接受任意异常
             */
            // 再次加入重试队列
            MqConsumeMessageDO freshMessage = mqLocalTransactionMessageService.getByMessageId(messageDO.getMessageId());
            if (freshMessage != null) {
                sendToRetryQueue(freshMessage);
            }
        } finally {
            // 任务执行完成后，删除去重容器、减少任务数
            retryMessageIdSet.remove(messageId);
            int remain = retryTaskCount.decrementAndGet();
            log.info("消费重试任务执行完成，mqConsumeMessageId:{}, 剩余任务数:{}", messageId, remain);
            // 释放分布式锁
            if (locked) {
                lock.unlock();
            }
        }
    }

    private boolean isRetryException(Exception e) {

        if (e instanceof MessageRetryConsumeException) {

            return true;
        }
        return false;
    }
}
