package com.lanf.rocketmq.sevice;

import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.model.enums.MqSendMessageTypeEnum;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * MQ消息发送服务
 * <p>封装MQ消息发送逻辑，提供统一的发送入口</p>
 */
@Slf4j
@Service
public class MqMessageSendService {

    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private IMqSendMessageService mqSendMessageService;

    @Autowired
    @Lazy
    private MqRetryService mqRetryService;

    /**
     * 发送MQ消息，失败时加入重试队列
     *
     * @param messageDO 消息记录
     */
    public void sendMessage(MqSendMessageDO messageDO) {
        try {
            doSend(messageDO);
            updateMessageStatus(messageDO);
            log.info("MQ消息发送成功，messageId:{}, topic:{}", messageDO.getId(), messageDO.getTopic());
        } catch (Exception e) {
            log.error("MQ消息发送失败，加入重试队列，messageId:{}, topic:{}", messageDO.getId(), messageDO.getTopic(), e);
            mqRetryService.addToRetryQueue(messageDO, 0);
        }
    }

    /**
     * 根据消息类型执行发送
     *
     * @param messageDO 消息记录
     */
    public void doSend(MqSendMessageDO messageDO) {
        MqSendMessageTypeEnum type = messageDO.getSendMessageType();
        if (type == null) {
            log.warn("消息类型为空，跳过发送，messageId:{}", messageDO.getId());
            return;
        }

        switch (type) {
            case NORMAL:
                rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                break;
            case ORDERED:
                if (messageDO.getMessageKey() == null) {
                    log.warn("顺序消息缺少messageKey，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    rocketMqClient.syncSendOrderly(messageDO.getTopic(), messageDO.getMessageContent(), messageDO.getMessageKey());
                }
                break;
            case NORMAL_TAG:
                if (messageDO.getTag() == null) {
                    log.warn("带Tag消息缺少tag，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    rocketMqClient.sendMessageWithTags(messageDO.getTopic(), messageDO.getTag(), messageDO.getMessageContent());
                }
                break;
            case ORDERED_TAG:
                if (messageDO.getTag() == null || messageDO.getMessageKey() == null) {
                    log.warn("顺序带Tag消息缺少tag或messageKey，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    rocketMqClient.sendOrderlyMessageWithTags(messageDO.getTopic(), messageDO.getTag(), messageDO.getMessageContent(), messageDO.getMessageKey());
                }
                break;
            case DELAY_NORMAL:
                if (messageDO.getDelayTime() == null) {
                    log.warn("延迟消息缺少delayTime，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    rocketMqClient.sendDelayMessage(messageDO.getTopic(), messageDO.getMessageContent(), TimeUnit.MINUTES, messageDO.getDelayTime());
                }
                break;
            case DELAY_ORDERED:
                if (messageDO.getDelayTime() == null || messageDO.getMessageKey() == null) {
                    log.warn("延迟顺序消息缺少delayTime或messageKey，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    String delayContent = messageDO.getMessageContent();
                    rocketMqClient.sendDelayMessage(messageDO.getTopic(), delayContent, TimeUnit.MINUTES, messageDO.getDelayTime());
                }
                break;
            case DELAY_NORMAL_TAG:
                if (messageDO.getDelayTime() == null || messageDO.getTag() == null) {
                    log.warn("延迟带Tag消息缺少delayTime或tag，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    String delayContent = messageDO.getMessageContent();
                    rocketMqClient.sendDelayMessage(messageDO.getTopic() + ":" + messageDO.getTag(), delayContent, TimeUnit.MINUTES, messageDO.getDelayTime());
                }
                break;
            case DELAY_ORDERED_TAG:
                if (messageDO.getDelayTime() == null || messageDO.getTag() == null || messageDO.getMessageKey() == null) {
                    log.warn("延迟顺序带Tag消息缺少必要参数，降级为普通消息发送，messageId:{}", messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                } else {
                    String delayContent = messageDO.getMessageContent();
                    rocketMqClient.sendDelayMessage(messageDO.getTopic() + ":" + messageDO.getTag(), delayContent, TimeUnit.MINUTES, messageDO.getDelayTime());
                }
                break;
            default:
                log.warn("未知的消息类型:{}, 降级为普通消息发送，messageId:{}", type, messageDO.getId());
                rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                break;
        }
    }

    /**
     * 更新消息状态为发送成功
     *
     * @param messageDO 消息记录
     */
    public void updateMessageStatus(MqSendMessageDO messageDO) {
        try {
            messageDO.setStatus(1);
            messageDO.setRetryCount(1);
            mqSendMessageService.updateById(messageDO);
        } catch (Exception e) {
            log.error("更新MQ消息状态失败，messageId:{}", messageDO.getId(), e);
        }
    }
}
