package com.lanf.order.mq.listener;

import com.lanf.common.utils.JsonUtils;
import com.lanf.order.service.order.IMainOrderService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 交易成功事件 - 订单服务消费者
 * 更新订单状态
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
    @Autowired
    private IMainOrderService mainOrderService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;
    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(TradeSuccessEventMessage message) {

        log.info("交易单更新成功消息,更新订单状态为已支付开始[{}]", JsonUtils.toJsonString(message));
        orderService.tradeSuccessEvent(message);

    }


}
