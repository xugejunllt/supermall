package com.lanf.order.mq.listener;

import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.mq.message.SecKillOrderConfirmMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.SEC_KILL_ORDER_CONFIRM_TOPIC,
        consumerGroup = OrderMqGroupName.SEC_KILL_ORDER_CONFIRM_GROUP)
public class SecKillOrderConfirmListener implements RocketMQListener<SecKillOrderConfirmMessage> {


    @Override
    public void onMessage(SecKillOrderConfirmMessage message) {

    }
}
