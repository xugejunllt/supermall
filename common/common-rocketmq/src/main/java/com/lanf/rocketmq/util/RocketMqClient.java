package com.lanf.rocketmq.util;

import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.model.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class RocketMqClient {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendMessage(String topic, Object message){

        log.info("发送mq消息:topic:{},message:{}",topic, JsonUtils.toJsonString(message));
        rocketMQTemplate.convertAndSend(topic, message);
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
