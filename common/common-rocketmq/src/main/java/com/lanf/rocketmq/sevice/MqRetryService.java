package com.lanf.rocketmq.sevice;

import com.lanf.cache.service.DistributedLocker;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MQ消息重试服务
 * <p>基于 HashedWheelTimer 实现延迟重试，最大重试3次</p>
 */
@Slf4j
@Service
public class MqRetryService {

    /**
     * 第1次重试延迟：5秒
     */
    private static final long DELAY_5S_MS = 5 * 1000L;

    /**
     * 第2次重试延迟：1分钟
     */
    private static final long DELAY_1M_MS = 60 * 1000L;

    /**
     * 第3次重试延迟：5分钟
     */
    private static final long DELAY_5M_MS = 5 * 60 * 1000L;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

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
    private final Set<Long> retryMessageIdSet = ConcurrentHashMap.newKeySet();

    /**
     * 延迟定时器
     */
    private static final HashedWheelTimer TIMER = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    @Autowired
    @Qualifier("mqRetrySendExecutor")
    private Executor mqSendExecutor;

    @Autowired
    private MqMessageSendService mqMessageSendService;

    @Autowired
    private IMqSendMessageService mqSendMessageService;

    @Autowired
    private DistributedLocker distributedLocker;

    /**
     * 将消息加入重试队列（HashedWheelTimer 延迟执行）
     *
     * @param messageDO   消息记录
     * @param retryCount  重试次数（1=首次重试，2=第二次重试，3=第三次重试）
     */
    public void addToRetryQueue(MqSendMessageDO messageDO, int retryCount) {
        if (retryCount > MAX_RETRY_COUNT) {

            return;
        }

        // 1. 去重检查
        Long messageId = messageDO.getId();
        if (!retryMessageIdSet.add(messageId)) {
            log.warn("消息已在重试队列中，跳过重复添加，messageId:{}", messageId);
            return;
        }

        // 2. 任务数上限检查
        int currentCount = retryTaskCount.incrementAndGet();
        if (currentCount > MAX_RETRY_TASK_COUNT) {
            retryTaskCount.decrementAndGet();
            retryMessageIdSet.remove(messageId);
            sendDingTalkAlert( messageDO);
            return;
        }

        long delayMillis = getDelayMillis(retryCount);

        log.info("消息已加入重试队列，messageId:{}, retryCount:{}, delay:{}ms",
                messageId, retryCount, delayMillis);

        TIMER.newTimeout(timeout -> {
            mqSendExecutor.execute(() -> doRetry(messageDO, retryCount, messageId));
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重试
     *
     * @param messageDO   消息记录
     * @param retryCount  当前重试次数
     * @param messageId   消息ID（用于去重和计数）
     */
    private void doRetry(MqSendMessageDO messageDO, int retryCount, Long messageId) {
        // 以 messageId 作为分布式锁key，防止同一消息并发重试
        String lockKey = "mq:retry:lock:" + messageId;
        boolean locked = false;
        try {
            locked = distributedLocker.getLock(lockKey);
            if (!locked) {
                log.warn("获取分布式锁失败，跳过本次重试，messageId:{}", messageId);
                return;
            }
            String shardingKey = messageDO.getShardingKey();

            MqSendMessageDO sendMessageDO = mqSendMessageService.lambdaQuery()
                    .eq(BaseEntity::getId, messageDO.getId())
                    .eq(shardingKey != null, MqSendMessageDO::getShardingKey, shardingKey)
                    .one();
            Integer retryCount1 = sendMessageDO.getRetryCount();
            if (retryCount1 >= MAX_RETRY_COUNT) {
                sendDingTalkAlert(messageDO);
                return;
            }

            Date nextEstimatedCompletionAt = getNextEstimatedCompletionAt(retryCount);

            boolean update = mqSendMessageService.lambdaUpdate()
                    .eq(BaseEntity::getId, messageDO.getId())
                    .eq(shardingKey != null, MqSendMessageDO::getShardingKey, shardingKey)
                    .set(MqSendMessageDO::getRetryCount, retryCount)
                    .set(MqSendMessageDO::getNextEstimatedCompletionAt, nextEstimatedCompletionAt)
                    .update();
            if (!update) {
                log.warn("消息已更新，跳过本次重试，messageId:{}", messageId);
               throw new MessageRetryConsumeException("更新失败");
            }

            // 调用 MqMessageSendService.sendMessage 重新发送
            // 内部包含 doSend + updateMessageStatus，失败会自动再次加入重试队列
            mqMessageSendService.sendMessage(messageDO, retryCount);
        } catch (Exception e) {
            log.error("MQ消息重试执行异常，messageId:{}, retryCount:{}", messageId, retryCount, e);
        } finally {
            // 任务执行完成后，删除去重容器、减少任务数、释放锁
            retryMessageIdSet.remove(messageId);
            int remain = retryTaskCount.decrementAndGet();
            if (locked) {
                distributedLocker.unlock(lockKey);
            }
            log.info("重试任务执行完成，messageId:{}, 剩余任务数:{}", messageId, remain);
        }
    }

    /**
     * 根据重试次数获取延迟时间
     *
     * @param retryCount 重试次数
     * @return 延迟毫秒数
     */
    private long getDelayMillis(int retryCount) {
        switch (retryCount) {
            case 1:
                return DELAY_5S_MS;
            case 2:
                return DELAY_1M_MS;
            case 3:
                return DELAY_5M_MS;
            default:
                return 0;
        }
    }
    public Date getNextEstimatedCompletionAt(int retryCount){

        long delayMillis = getDelayMillis(retryCount + 1);

        return new Date(System.currentTimeMillis() + delayMillis);
    }
    /**
     * 发送钉钉告警（伪代码）
     *
     * @param messageDO 消息记录
     */
    private void sendDingTalkAlert(MqSendMessageDO messageDO) {
        log.error("【钉钉告警】MQ消息多次发送失败，messageId:{}, topic:{}",
                messageDO.getId(), messageDO.getTopic());
        // TODO: 调用钉钉Webhook API 发送告警
    }
}
