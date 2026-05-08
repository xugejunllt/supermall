package com.lanf.seckill.mq.listener;

import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillSuccessMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillMqTopicName.SEC_KILL_SUCCESS_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_SUCCESS_GROUP
)
public class SecKillSuccessListener implements RocketMQListener<SecKillSuccessMessage> {

    @Override
    public void onMessage(SecKillSuccessMessage message) {
        log.info("收到秒杀成功消息: userId={}, secKillId={}, orderNumber={}",
                message.getUserId(), 
                message.getSecKillId(),
                message.getOrderNumber());

    }


}
