package com.lanf.rocketmq.util;

import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.StackTraceUtil;
import com.lanf.constant.utils.TraceIdUtils;
import io.netty.util.HashedWheelTimer;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RocketMqClient {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    private static final HashedWheelTimer TIMER = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    /**
     * 构建带链路 ID 的消息头
     *
     * @return 消息头 Map
     */
    private Map<String, Object> buildHeadersWithTraceId() {
        Map<String, Object> headers = new HashMap<>();
        String traceId = TraceIdUtils.getTraceId();
        if (traceId != null) {
            headers.put("traceId", traceId);
        }
        return headers;
    }

    /**
     * 发送顺序消息
     *
     *
     *
     */
    public void syncSendOrderly(String topic, String message, String key){
        log.info("发送顺序mq消息开始:topic:{},message:{}", topic, message);
        
        Map<String, Object> headers = buildHeadersWithTraceId();
        Message<Object> messageWithHeader = MessageBuilder.createMessage(message, new MessageHeaders(headers));
        rocketMQTemplate.syncSendOrderly(topic, messageWithHeader, key);
    }

    /**
     * 发送普通消息
     *
     *
     */
    public void sendMessage(String topic, String message){

        log.info("发送mq消息开始:topic:{},message:{}", topic, message);

        try {
            Map<String, Object> headers = buildHeadersWithTraceId();
            Message<Object> messageWithHeader = MessageBuilder.createMessage(message, new MessageHeaders(headers));
            SendResult sendResult = rocketMQTemplate.syncSend(topic, messageWithHeader);

             if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
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
            Map<String, Object> headers = buildHeadersWithTraceId();
            Message<Object> messageWithHeader = MessageBuilder.createMessage(message, new MessageHeaders(headers));
            SendResult sendResult = rocketMQTemplate.syncSend(destination, messageWithHeader);

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
            Map<String, Object> headers = buildHeadersWithTraceId();
            Message<Object> messageWithHeader = MessageBuilder.createMessage(message, new MessageHeaders(headers));
            SendResult sendResult = rocketMQTemplate.syncSendOrderly(destination, messageWithHeader, hashKey);

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
     * 通过时钟轮算法实现
     *
     */
    public void sendDelayMessage(String topic, String message, TimeUnit timeUnit, int delayTime){

        log.info("发送延迟mq消息开始:topic:{},message:{},timeUnit:{},delayTime:{}", topic, message, timeUnit, delayTime);
        
        String traceId = TraceIdUtils.getTraceId();
        
        TIMER.newTimeout(timeout -> {
            try {
                log.info("延迟时间到，开始发送MQ消息:topic:{}", topic);
                
                if (traceId != null) {
                    TraceIdUtils.setTraceId(traceId);
                }
                
                Map<String, Object> headers = buildHeadersWithTraceId();
                Message<Object> messageWithHeader = MessageBuilder.createMessage(message, new MessageHeaders(headers));
                SendResult sendResult = rocketMQTemplate.syncSend(topic, messageWithHeader);

                if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    String sendResultJson = JsonUtils.toJsonString(sendResult);
                    log.error("发送MQ消息失败,异常状态[{}]", sendResultJson);
                } else {
                    log.info("发送mq消息成功:topic:{}", topic);
                }

            } catch (Exception e) {
                log.error("发送MQ消息失败,topic:{}", topic, e);
            } finally {
                TraceIdUtils.clearAll();
            }

        }, delayTime, timeUnit);

    }
    public void sendMessage(String topic, Object message){

        String jsonMessage = JsonUtils.toJsonString(message);
        log.info("发送mq消息:topic:{},message:{}", topic, jsonMessage);

        try {
            Map<String, Object> headers = buildHeadersWithTraceId();
            Message<Object> messageWithHeader = MessageBuilder.createMessage(jsonMessage, new MessageHeaders(headers));
            SendResult sendResult = rocketMQTemplate.syncSend(topic, messageWithHeader);
        } catch (Exception e) {

            //不抛异常，事务最终一致性
            log.error("发送MQ消息失败[{}]", StackTraceUtil.getStackTrace(e));
        }

    }







}
