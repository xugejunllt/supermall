package com.lanf.rocketmq.sevice;

import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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

    /**
     * 将消息加入重试队列（HashedWheelTimer 延迟执行）
     *
     * @param messageDO   消息记录
     * @param retryCount  重试次数（1=首次重试，2=第二次重试，3=第三次重试）
     */
    public void addToRetryQueue(MqSendMessageDO messageDO, int retryCount) {
        if (retryCount > MAX_RETRY_COUNT) {
            sendDingTalkAlert(messageDO);
            return;
        }

        long delayMillis = getDelayMillis(retryCount);

        log.info("消息已加入重试队列，messageId:{}, retryCount:{}, delay:{}ms",
                messageDO.getId(), retryCount, delayMillis);

        TIMER.newTimeout(timeout -> {
            mqSendExecutor.execute(() -> doRetry(messageDO, retryCount));
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重试
     *
     * @param messageDO   消息记录
     * @param retryCount  当前重试次数
     */
    private void doRetry(MqSendMessageDO messageDO, int retryCount) {
        // 更新重试次数到 DB
        messageDO.setRetryCount(retryCount);
        mqSendMessageService.updateById(messageDO);

        // 调用 MqMessageSendService.sendMessage 重新发送
        // 内部包含 doSend + updateMessageStatus，失败会自动再次加入重试队列
        mqMessageSendService.sendMessage(messageDO, retryCount);
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
