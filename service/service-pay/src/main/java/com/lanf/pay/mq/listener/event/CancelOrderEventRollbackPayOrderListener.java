package com.lanf.pay.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@RocketMQMessageListener(topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = PayMqGroupName.CANCEL_ORDER_CANCEL_PAY_ORDER_GROUP,
        selectorExpression = OrderTopicWithTag.TAG_CANCELLED
)

public class CancelOrderEventRollbackPayOrderListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private IPaymentCancelRecordService paymentCancelRecordService;
    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IPrepayPayTypeService prepayPayTypeService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(CancelOrderEventMessage message) {

        log.info("取消订单消息,回滚三方支付订单开始:{}", JsonUtils.toJsonString(message));

        Long orderId = message.getOrderId();
        z


    }


}