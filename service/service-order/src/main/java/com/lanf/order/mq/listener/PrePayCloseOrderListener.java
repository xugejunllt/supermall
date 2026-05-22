package com.lanf.order.mq.listener;

import com.lanf.order.service.order.IOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PrePayMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.PRE_PAY_CLOSE_ORDER_TOPIC, consumerGroup = TopicName.PROMISE_ORDER_RETURN_MONEY_GROUP)
public class PrePayCloseOrderListener implements RocketMQListener<PrePayMsg> {

    @Autowired
    private IOrderService orderService;

    @Override
    public void onMessage(PrePayMsg message) {

        log.info("创建支付订单后进行订单关闭:{}", message);

        orderService.closeTimeOutNotPayOrder(message.getOrderId());

    }
}