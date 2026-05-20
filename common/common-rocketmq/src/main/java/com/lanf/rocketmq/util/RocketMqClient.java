package com.lanf.rocketmq.util;

import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.StackTraceUtil;
import com.lanf.rocketmq.model.BaseMessage;
import com.lanf.rocketmq.model.enums.DelayLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import io.netty.util.HashedWheelTimer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RocketMqClient {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    private static final HashedWheelTimer TIMER = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    /**
     * 发送顺序消息
     *
     *
     *
     */
    public void syncSendOrderly(String topic, String message,String key){
        log.info("发送顺序mq消息开始:topic:{},message:{}",topic, message);
        rocketMQTemplate.syncSendOrderly(topic, message, key);
    }

    /**
     * 发送普通消息
     *
     *
     */
    public void sendMessage(String topic, String message){

        log.info("发送mq消息开始:topic:{},message:{}",topic, message);

        try {
            SendResult  sendResult = rocketMQTemplate.syncSend(topic, message);

             if ( !SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                 String sendResultJson = JsonUtils.toJsonString(sendResult);
                 log.error("发送MQ消息失败,异常状态[{}]", sendResultJson);
             } else {
                 log.info("发送mq消息成功");

             }

        } catch (Exception e) {
            log.error("发送MQ消息失败" ,e);
        }

    }
    /**
     * 发送带Tag的消息（同步）
     * Topic格式: topic:tag
     *
     * @param topic 主题
     * @param tag 标签（用于消息过滤）
     * @param message 消息内容（JSON字符串）
     */
    public void sendMessageWithTags(String topic, String tag, String message){

        String destination = topic + ":" + tag;
        log.info("发送带Tag的mq消息开始:destination:{},tag:{},message:{}", destination, tag, message);

        try {
            SendResult sendResult = rocketMQTemplate.syncSend(destination, message);

            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                String sendResultJson = JsonUtils.toJsonString(sendResult);
                log.error("发送带Tag的MQ消息失败,异常状态[{}]", sendResultJson);
            } else {
                log.info("发送带Tag的mq消息成功,tag:{}", tag);
            }

        } catch (Exception e) {
            log.error("发送带Tag的MQ消息失败,destination:{},tag:{}", destination, tag, e);
        }

    }
    /**
     * 发送带Tag的顺序消息（同步）
     *
     * <p>通过 hashKey 确保同一业务实体（如同一订单）的消息进入同一个队列，
     * 从而保证消费者按发送顺序处理消息。
     *
     * @param topic 主题
     * @param tag 标签（用于消息过滤）
     * @param message 消息内容（JSON字符串）
     * @param hashKey 哈希键（如 orderId），用于路由到特定队列
     */
    public void sendOrderlyMessageWithTags(String topic, String tag, String message, String hashKey) {
        String destination = topic + ":" + tag;
        log.info("发送带Tag的顺序mq消息开始:destination:{},tag:{},hashKey:{},message:{}", destination, tag, hashKey, message);

        try {
            // 使用 syncSendOrderly 方法，传入 hashKey
            SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination, message, hashKey);

            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                String sendResultJson = JsonUtils.toJsonString(sendResult);
                log.error("发送带Tag的顺序MQ消息失败,异常状态[{}], hashKey:{}", sendResultJson, hashKey);
            } else {
                log.info("发送带Tag的顺序mq消息成功,tag:{}, hashKey:{}, queueId:{}", tag, hashKey, sendResult.getMessageQueue().getQueueId());
            }

        } catch (Exception e) {
            log.error("发送带Tag的顺序MQ消息失败,destination:{},tag:{},hashKey:{}", destination, tag, hashKey, e);
        }
    }
    /**
     * 发送延迟消息
     *
     */
    public void sendDelayMessage(String topic, String message, DelayLevelEnum delayLevel) {

        log.info("发送延迟mq消息开始:topic:{},delayLevel:{},message:{}", topic, delayLevel.getDescription(), message);
        MessageHeaders messageHeaders = new MessageHeaders(new HashMap<>());
        Message<Object> message1 = MessageBuilder.createMessage(message, messageHeaders);
        try {
            SendResult sendResult = rocketMQTemplate.syncSend(topic, message1, 0, delayLevel.getLevel());

            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                String sendResultJson = JsonUtils.toJsonString(sendResult);
                log.error("发送延迟MQ消息失败,异常状态[{}]", sendResultJson);
            } else {
                log.info("发送延迟mq消息成功,delayLevel:{}", delayLevel.getDescription());
            }

        } catch (Exception e) {
            log.error("发送延迟MQ消息失败,topic:{},delayLevel:{}", topic, delayLevel.getDescription(), e);
        }

    }
    /**
     * 发送延迟消息
     * 通过时钟轮算法实现
     *
     */
    public void sendDelayMessage(String topic, String message, TimeUnit timeUnit, int delayTime){

        log.info("发送延迟mq消息开始:topic:{},message:{},timeUnit:{},delayTime:{}秒", topic, message, timeUnit, delayTime);
        
        TIMER.newTimeout(timeout -> {
            try {
                log.info("延迟时间到，开始发送MQ消息:topic:{}", topic);
                SendResult sendResult = rocketMQTemplate.syncSend(topic, message);

                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    String sendResultJson = JsonUtils.toJsonString(sendResult);
                    log.error("发送MQ消息失败,异常状态[{}]", sendResultJson);
                } else {
                    log.info("发送mq消息成功:topic:{}", topic);
                }

            } catch (Exception e) {
                log.error("发送MQ消息失败,topic:{}", topic, e);
            }

        }, delayTime, timeUnit);

    }
    public void sendMessage(String topic, Object message){

        String jsonMessage = JsonUtils.toJsonString(message);
        log.info("发送mq消息:topic:{},message:{}",topic, jsonMessage);

        try {
            SendResult sendResult = rocketMQTemplate.syncSend(topic, jsonMessage);
        } catch (Exception e) {

            //不抛异常，事务最终一致性
            log.error("发送MQ消息失败[{}]", StackTraceUtil.getStackTrace(e));
        }

    }


    public void sendMessage(String topic, BaseMessage message)  {

        log.info("发送mq消息:topic:{},message:{}",topic, JsonUtils.toJsonString(message));

        MessageHeaders messageHeaders = new MessageHeaders(new HashMap<>());
        Message<Object> message1 = MessageBuilder.createMessage(JsonUtils.toJsonString(message), messageHeaders);
        SendResult sendResult = rocketMQTemplate.syncSend(topic, message1, 0, message.getDelayTime());

        if ( !SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            log.error("发送失败");
        }
    }






}
