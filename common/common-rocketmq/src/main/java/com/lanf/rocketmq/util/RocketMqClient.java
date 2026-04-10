package com.lanf.rocketmq.util;

import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.StackTraceUtil;
import com.lanf.rocketmq.model.BaseMessage;
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

@Slf4j
@Service
public class RocketMqClient {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送顺序消息
     * @param topic
     * @param message
     * @param key
     */
    public void syncSendOrderly(String topic, String message,String key){

        rocketMQTemplate.syncSendOrderly(topic, message, key);
    }


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
            log.error("发送MQ消息失败[{}]" ,e);
        }

    }
    private void  handleException(String  sendResultJson,String exception){





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
            //发送成功
            log.error("发送失败");
        }
    }


}
