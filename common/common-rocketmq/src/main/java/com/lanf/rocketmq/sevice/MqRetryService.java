package com.lanf.rocketmq.sevice;

import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.model.dto.MqRetryMessageDTO;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * MQ消息重试服务
 * <p>管理消息重试队列，支持延迟重试和钉钉告警</p>
 */
@Slf4j
@Service
public class MqRetryService {

    /**
     * 重试队列Redis Key
     */
    private static final String RETRY_QUEUE_KEY = "mq:retry:queue";

    /**
     * 重试分布式锁Redis Key
     */
    private static final String RETRY_LOCK_KEY = "mq:retry:lock";

    /**
     * 首次重试延迟：5秒
     */
    private static final long DELAY_5S_MS = 5 * 1000L;

    /**
     * 第二次重试延迟：1分钟
     */
    private static final long DELAY_1M_MS = 60 * 1000L;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private MqMessageSendService mqMessageSendService;

    /**
     * 将消息加入重试队列
     *
     * @param messageDO   消息记录
     * @param retryCount  重试次数（0=首次重试，1=第二次重试）
     */
    public void addToRetryQueue(MqSendMessageDO messageDO, int retryCount) {
        try {
            RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(RETRY_QUEUE_KEY);
            MqRetryMessageDTO retryMessage = new MqRetryMessageDTO();
            retryMessage.setMessage(messageDO);
            retryMessage.setRetryCount(retryCount);

            long nextRetryTime = System.currentTimeMillis() + (retryCount == 0 ? DELAY_5S_MS : DELAY_1M_MS);
            sortedSet.add(nextRetryTime, JsonUtils.toJsonString(retryMessage));

            log.info("消息已加入重试队列，messageId:{}, retryCount:{}, nextRetryTime:{}",
                    messageDO.getId(), retryCount, nextRetryTime);
        } catch (Exception e) {
            log.error("加入重试队列失败，messageId:{}", messageDO.getId(), e);
        }
    }

    /**
     * 定时重试发送消息，每5秒执行一次
     */
    @Scheduled(fixedDelay = 5000)
    public void retrySend() {
        RLock lock = redissonClient.getLock(RETRY_LOCK_KEY);
        if (!lock.tryLock()) {
            return;
        }
        try {
            RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(RETRY_QUEUE_KEY);
            Collection<String> messages = sortedSet.valueRange(0, true, System.currentTimeMillis(), true);

            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (String json : messages) {
                sortedSet.remove(json);

                MqRetryMessageDTO retryMessage = JsonUtils.toObject(json, MqRetryMessageDTO.class);
                if (retryMessage == null || retryMessage.getMessage() == null) {
                    log.warn("重试消息解析失败，跳过: {}", json);
                    continue;
                }

                MqSendMessageDO messageDO = retryMessage.getMessage();
                int retryCount = retryMessage.getRetryCount();

                try {
                    mqMessageSendService.doSend(messageDO);
                    mqMessageSendService.updateMessageStatus(messageDO);
                    log.info("MQ消息重试发送成功，messageId:{}, retryCount:{}", messageDO.getId(), retryCount);
                } catch (Exception e) {
                    log.error("MQ消息重试发送失败，messageId:{}, retryCount:{}", messageDO.getId(), retryCount, e);
                    if (retryCount >= 1) {
                        sendDingTalkAlert(retryMessage);
                    } else {
                        addToRetryQueue(messageDO, retryCount + 1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("重试定时任务执行异常", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 发送钉钉告警（伪代码）
     *
     * @param retryMessage 重试消息
     */
    private void sendDingTalkAlert(MqRetryMessageDTO retryMessage) {
        MqSendMessageDO message = retryMessage.getMessage();
        log.error("【钉钉告警】MQ消息多次发送失败，messageId:{}, topic:{}",
                message.getId(), message.getTopic());
        // TODO: 调用钉钉Webhook API 发送告警
    }
}
