package com.lanf.rocketmq.util;

import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.model.enums.MqSendMessageTypeEnum;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import com.lanf.rocketmq.sevice.MqRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MQ消息发送工具类
 * <p>支持将消息暂存到内存队列，在事务提交后统一发送到消息队列</p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Slf4j
@Component
public class MqSendMessageUtils {

    /**
     * 线程本地内存队列，用于暂存待发送的MQ消息
     */
    private static final ThreadLocal<Queue<MqSendMessageDO>> MESSAGE_QUEUE = ThreadLocal.withInitial(ConcurrentLinkedQueue::new);

    @Autowired
    private IMqSendMessageService mqSendMessageService;
    @Autowired
    private MqRetryService mqRetryService;

    /**
     * 发送MQ消息（普通消息）
     *
     * @param topic   消息主题
     * @param content 消息内容
     */
    public void sendMessage(String topic, String content) {
        sendMessage(topic, null, MqSendMessageTypeEnum.NORMAL, content, null, null);
    }

    /**
     * 发送MQ消息（带Tag）
     *
     * @param topic   消息主题
     * @param tag     消息标签
     * @param content 消息内容
     */
    public void sendMessageWithTag(String topic, String tag, String content) {
        sendMessage(topic, tag, MqSendMessageTypeEnum.NORMAL_TAG, content, null, null);
    }

    /**
     * 发送顺序MQ消息
     *
     * @param topic      消息主题
     * @param content    消息内容
     * @param messageKey 顺序消息Key
     */
    public void sendOrderedMessage(String topic, String content, String messageKey) {
        sendMessage(topic, null, MqSendMessageTypeEnum.ORDERED, content, null, messageKey);
    }

    /**
     * 发送顺序MQ消息（带Tag）
     *
     * @param topic      消息主题
     * @param tag        消息标签
     * @param content    消息内容
     * @param messageKey 顺序消息Key
     */
    public void sendOrderedMessageWithTag(String topic, String tag, String content, String messageKey) {
        sendMessage(topic, tag, MqSendMessageTypeEnum.ORDERED_TAG, content, null, messageKey);
    }

    /**
     * 发送延迟MQ消息
     *
     * @param topic    消息主题
     * @param content  消息内容
     * @param delayTime 延迟时间（分钟）
     */
    public void sendDelayMessage(String topic, String content, Integer delayTime) {
        sendMessage(topic, null, MqSendMessageTypeEnum.DELAY_NORMAL, content, delayTime, null);
    }

    /**
     * 发送延迟MQ消息（带Tag）
     *
     * @param topic     消息主题
     * @param tag       消息标签
     * @param content   消息内容
     * @param delayTime 延迟时间（分钟）
     */
    public void sendDelayMessageWithTag(String topic, String tag, String content, Integer delayTime) {
        sendMessage(topic, tag, MqSendMessageTypeEnum.DELAY_NORMAL_TAG, content, delayTime, null);
    }

    /**
     * 发送延迟顺序MQ消息
     *
     * @param topic      消息主题
     * @param content    消息内容
     * @param delayTime  延迟时间（分钟）
     * @param messageKey 顺序消息Key
     */
    public void sendDelayOrderedMessage(String topic, String content, Integer delayTime, String messageKey) {
        sendMessage(topic, null, MqSendMessageTypeEnum.DELAY_ORDERED, content, delayTime, messageKey);
    }

    /**
     * 发送延迟顺序MQ消息（带Tag）
     *
     * @param topic      消息主题
     * @param tag        消息标签
     * @param content    消息内容
     * @param delayTime  延迟时间（分钟）
     * @param messageKey 顺序消息Key
     */
    public void sendDelayOrderedMessageWithTag(String topic, String tag, String content, Integer delayTime, String messageKey) {
        sendMessage(topic, tag, MqSendMessageTypeEnum.DELAY_ORDERED_TAG, content, delayTime, messageKey);
    }

    /**
     * 通用的消息发送方法，将消息封装成MqSendMessageDO插入内存队列和数据库
     *
     * @param topic           消息主题
     * @param tag             消息标签
     * @param sendMessageType 消息类型
     * @param content         消息内容
     * @param delayTime       延迟时间（分钟）
     * @param messageKey      顺序消息Key
     */
    private void sendMessage(String topic, String tag, MqSendMessageTypeEnum sendMessageType,
                             String content, Integer delayTime, String messageKey) {

        Date nextEstimatedCompletionAt = mqRetryService.getNextEstimatedCompletionAt(0);

        MqSendMessageDO messageDO = new MqSendMessageDO();
        messageDO.setTopic(topic);
        messageDO.setTag(tag);
        messageDO.setSendMessageType(sendMessageType);
        messageDO.setMessageContent(content);
        messageDO.setDelayTime(delayTime);
        messageDO.setMessageKey(messageKey);
        messageDO.setStatus(0);
        messageDO.setRetryCount(0);
        messageDO.setNextEstimatedCompletionAt(nextEstimatedCompletionAt);
        // 1. 保存到数据库
        boolean saved = mqSendMessageService.save(messageDO);
        if (!saved) {
            log.error("保存MQ发送消息记录失败, topic:{}", topic);
            return;
        }

        // 2. 放入内存队列
        Queue<MqSendMessageDO> queue = MESSAGE_QUEUE.get();
        queue.offer(messageDO);
        log.debug("MQ消息已加入内存队列, topic:{}, messageId:{}", topic, messageDO.getId());
    }

    /**
     * 获取并清空当前线程内存队列中的所有消息
     *
     * @return 内存队列中的消息列表
     */
    public static List<MqSendMessageDO> getAndClearMessages() {
        Queue<MqSendMessageDO> queue = MESSAGE_QUEUE.get();
        List<MqSendMessageDO> messages = new ArrayList<>();
        MqSendMessageDO message;
        while ((message = queue.poll()) != null) {
            messages.add(message);
        }
        return messages;
    }

    /**
     * 清除当前线程的内存队列
     */
    public static void clear() {
        MESSAGE_QUEUE.remove();
    }
}
