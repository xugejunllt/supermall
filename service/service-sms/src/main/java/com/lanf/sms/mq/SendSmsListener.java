package com.lanf.sms.mq;

import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import com.lanf.sms.service.biz.ITemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.SEND_SMS_TOPIC, consumerGroup = TopicName.SEND_SMS_GROUP)
public class SendSmsListener implements RocketMQListener<SendSmsMsg> {


    @Autowired
    private ITemplateService templateService;

    /**
     * 退款
     */
    @Override
    public void onMessage(SendSmsMsg message) {

        log.info("单条短信发送:message{}:",message);
        try {
            templateService.sendSms(message);
        }catch (Exception e){
            e.printStackTrace();

        }

    }

}