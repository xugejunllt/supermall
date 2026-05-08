package com.lanf.seckill.mq.listener;

import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.message.SecKillOrderCreatedMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderClientTopicName.SEC_KILL_ORDER_CREATED_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_STATUS_UPDATE_TOPIC
)
public class SecKillOrderCreatedListener implements RocketMQListener<SecKillOrderCreatedMessage> {


    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(SecKillOrderCreatedMessage message) {




    }




}
