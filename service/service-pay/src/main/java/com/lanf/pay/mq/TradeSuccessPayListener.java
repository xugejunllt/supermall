package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.enums.PaySceneEnum;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 交易成功事件 - 支付服务消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = TopicName.TRADE_SUCCESS_EVENT_TOPIC, 
    consumerGroup = TopicName.TRADE_SUCCESS_PAY_GROUP
)
public class TradeSuccessPayListener implements RocketMQListener<TradeSuccessEventMessage> {

    @Autowired
    private ITradeOrderService tradeOrderService;

    @Override
    public void onMessage(TradeSuccessEventMessage message) {






    }
}
