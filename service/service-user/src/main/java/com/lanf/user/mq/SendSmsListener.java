package com.lanf.user.mq;

import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.SEND_SMS_TOPIC, consumerGroup = TopicName.SEND_SMS_GROUP,maxReconsumeTimes = TopicName.MAX_RECONSUME_TIMES)
public class SendSmsListener implements RocketMQListener<SendSmsMsg> {

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(SendSmsMsg message) {

        log.info("监听到短信消息:message{}:",message);
        throw new MessageRetryConsumeException("模拟短信发送异常");

    }

}