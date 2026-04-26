package com.lanf.order.mq;

import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PromiseOrderReturnMoneyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.PROMISE_ORDER_RETURN_MONEY_TOPIC, consumerGroup = TopicName.PRE_PAY_CLOSE_ORDER_GROUP)
public class PromiseOrderReturnMoneyListener implements RocketMQListener<PromiseOrderReturnMoneyDTO> {

    @Autowired
    private IPromiseOrderService promiseOrderService;


    @ConsumeMessage
    @Override
    public void onMessage(PromiseOrderReturnMoneyDTO message) {

        log.info("履约单退款事件监听:{}", message);

        promiseOrderService.returnMoney(message.getOrderId());

    }
}