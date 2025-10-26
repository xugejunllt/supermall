package com.lanf.order.mq;

import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.order.service.IPromiseOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PromiseOrderLiquidationMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.PROMISE_ORDER_LIQUIDATION_TOPIC, consumerGroup = TopicName.PROMISE_ORDER_LIQUIDATION_GROUP)
public class PromiseOrderLiquidationListener implements RocketMQListener<PromiseOrderLiquidationMsg> {

    @Autowired
    private IPromiseOrderService promiseOrderService;

    @ConsumeMessage
    @Override
    public void onMessage(PromiseOrderLiquidationMsg message) {

        log.info("履约完成，开始进行结算:{}", message);

        promiseOrderService.promiseOrderLiquidation(message.getOrderId());


    }
}