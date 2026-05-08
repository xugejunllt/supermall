package com.lanf.seckill.mq.listener;

import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillMqExecuteMessage;
import com.lanf.seckill.service.ISecKillItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillMqTopicName.SEC_KILL_MQ_EXECUTE_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_MQ_EXECUTE_GROUP
)
public class SecKillMqExecuteListener implements RocketMQListener<SecKillMqExecuteMessage> {

    @Autowired
    private ISecKillItemService secKillItemService;



    @Override
    public void onMessage(SecKillMqExecuteMessage message) {
        

    }
}
