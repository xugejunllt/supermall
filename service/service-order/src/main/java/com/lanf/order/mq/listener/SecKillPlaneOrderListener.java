package com.lanf.order.mq.listener;

import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.OrderManagerService;
import com.lanf.seckill.mq.constant.SecKillClientTopicName;
import com.lanf.seckill.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
        consumerGroup = OrderMqGroupName.SEC_KILL_PLANE_GROUP)
public class SecKillPlaneOrderListener implements RocketMQListener<SecKillPlaneMessage> {

    @Autowired
    private OrderManagerService orderManagerService;

    @Override
    public void onMessage(SecKillPlaneMessage message) {

        log.info("接收到秒杀单消息,创建秒杀订单开始：{}", message);
        orderManagerService.createSecKillOrder(message);

    }



}