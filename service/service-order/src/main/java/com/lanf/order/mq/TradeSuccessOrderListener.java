package com.lanf.order.mq.event;

import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.order.service.IOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 交易成功事件 - 订单服务消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = TopicName.TRADE_SUCCESS_EVENT_TOPIC, 
    consumerGroup = TopicName.TRADE_SUCCESS_ORDER_GROUP
)
public class TradeSuccessOrderListener implements RocketMQListener<TradeSuccessEventMessage> {

    @Autowired
    private IOrderService orderService;

    @ConsumeMessage
    @Override
    public void onMessage(TradeSuccessEventMessage message) {
        log.info("收到交易成功事件通知，订单服务处理:tradeOrderId={},outTradeNo={},bizOrderId={}", 
                message.getTradeOrderId(), message.getOutTradeNo(), message.getBizOrderId());

    }
}
