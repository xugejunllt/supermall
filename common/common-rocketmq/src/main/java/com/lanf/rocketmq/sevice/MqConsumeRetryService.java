package com.lanf.rocketmq.sevice;

import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MQ消费消息重试服务
 * <p>封装消费消息重试队列的入队和执行逻辑，供AOP切面和定时任务复用</p>
 */
@Slf4j
@Service
public class MqConsumeRetryService {

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

    /**
     * 最大重试任务数（队列中同时存在的最大任务数）
     */
    private static final int MAX_RETRY_TASK_COUNT = 10000;

    /**
     * 当前重试任务数统计
     */
    private final AtomicInteger retryTaskCount = new AtomicInteger(0);

    /**
     * 去重容器：已加入重试队列的消息ID
     */
    private final Set<String> retryMessageIdSet = ConcurrentHashMap.newKeySet();

    /**
     * 延迟定时器
     */
    private static final HashedWheelTimer TIMER = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    /**
     * 分布式锁前缀
     */
    private static final String LOCK_PREFIX = "mq:consume:";

    /**
     * 将消息加入消费重试队列（HashedWheelTimer 延迟执行）
     *
     * @param messageDO 消息记录
     */
    public void addToRetryQueue(MqConsumeMessageDO messageDO) {
        String messageId = messageDO.getMessageId();

        // 1. 去重检查
        if (!retryMessageIdSet.add(messageId)) {
            log.warn("消息已在重试队列中，跳过重复添加，messageId:{}", messageId);
            return;
        }

        // 2. 任务数上限检查
        int currentCount = retryTaskCount.incrementAndGet();
        if (currentCount > MAX_RETRY_TASK_COUNT) {
            retryTaskCount.decrementAndGet();
            retryMessageIdSet.remove(messageId);
            log.warn("重试任务数已达上限{}/{}，跳过添加，messageId:{}", currentCount, MAX_RETRY_TASK_COUNT, messageId);
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
            return;
        }

        long delayMillis = mqRetryStrategy.getDelayMillis(retryCount);

        log.info("消息加入重试队列，messageId:{}, retryCount:{}, delay:{}ms",
                messageDO.getMessageId(), retryCount, delayMillis);
        TIMER.newTimeout(timeout -> {
            mqRetrySendExecutor.execute(() -> doRetry(messageDO, retryCount, mqRetryStrategy));
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重试
     *
     * @param messageDO  消息记录
     * @param retryCount 重试次数
     * @param mqRetryStrategy 重试策略
     */
    private void doRetry(MqConsumeMessageDO messageDO, int retryCount, MqRetryStrategy mqRetryStrategy) {
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
            if (messageDO.getRetryCount() > mqRetryStrategy.maxRetryCount()) {
                log.error("钉钉告警");
                return;
            }

            Date nextEstimatedCompletionAt = new Date(System.currentTimeMillis() +
                    mqRetryStrategy.getDelayMillis(retryCount + 1));
            mqLocalTransactionMessageService.lambdaUpdate()
                    .eq(MqConsumeMessageDO::getMessageId, messageDO.getMessageId())
                    .set(MqConsumeMessageDO::getRetryCount,retryCount)
                    .set(MqConsumeMessageDO::getNextEstimatedCompletionAt,nextEstimatedCompletionAt)
                    .set(messageDO.getMaxRetryCount().equals(retryCount),MqConsumeMessageDO::getStatus,2)
                    .update();
            // 通过反射重新执行方法
            mqRetryReflectExecutor.execute(messageDO);
            // 反射执行成功，更新状态
            mqLocalTransactionMessageService.lambdaUpdate()
                    .eq(MqConsumeMessageDO::getMessageId, messageDO.getMessageId())
                    .set(MqConsumeMessageDO::getStatus,1)
                    .set(MqConsumeMessageDO::getErrorMsg,null)
                    .update();

            log.info("MQ消息重试消费成功，messageId:{}, retryCount:{}",
                    messageDO.getMessageId(), retryCount);
        } catch (Exception e) {
            log.error("MQ消息第{}次重试失败，messageId:{},", retryCount, messageDO.getMessageId(), e);
            // 再次加入重试队列
            MqConsumeMessageDO freshMessage = mqLocalTransactionMessageService.getByMessageId(messageDO.getMessageId());
            if (freshMessage != null) {
                retryMessageIdSet.remove(messageId);
                int remain = retryTaskCount.decrementAndGet();
                log.info("准备再次入队，先清除当前去重标识，messageId:{}, 剩余任务数:{}", messageId, remain);
                addToRetryQueue(freshMessage);
            }
        } finally {
            retryMessageIdSet.remove(messageId);
            int remain = retryTaskCount.decrementAndGet();
            log.info("消费重试任务执行完成，messageId:{}, 剩余任务数:{}", messageId, remain);
            if (locked) {
                lock.unlock();
            }
        }
    }
}
