package com.lanf.rocketmq.util;

import com.lanf.common.utils.BeanUtil;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsMsg;

/**
 * 封装一些常发的topic
 */
public class UsedTopicSend {

    public static void  sendSmsMessage(SendSmsMsg message ){

        RocketMqClient rocketMqClient = BeanUtil.getBean(RocketMqClient.class);
        rocketMqClient.sendMessage(TopicName.SEND_SMS_TOPIC,message);
    }

}
