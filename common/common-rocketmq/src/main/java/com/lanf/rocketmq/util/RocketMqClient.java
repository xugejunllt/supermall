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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RocketMqClient {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    // 1. 创建时间轮调度器（每格100ms，共512格，最大支持约51秒延迟）
    private HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);
    /**
     * 发送顺序消息
     *
     *
     *
     */
    public void syncSendOrderly(String topic, String message,String key){

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
    public void sendDelayMessage(String topic, String message,TimeUnit timeUnit, int delayTime){

        log.info("发送mq消息开始:topic:{},message:{}",topic, message);
        timer.newTimeout(() -> {
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
