package com.lanf.pay.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.pay.model.bo.CancelPayOrderContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.trade.PayMethodHandler;
import com.lanf.pay.service.trade.PayMethodHandlerFactory;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 取消订单时
 * 取消三方支付订单
 * 或者
 */

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

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(CancelOrderEventMessage message) {

        log.info("取消订单消息,回滚三方支付订单开始:{}", JsonUtils.toJsonString(message));

        Long orderId = message.getOrderId();

        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOrderId, orderId)
                .one();
        if (tradeOrderDO == null) {
            log.error("交易单不存在");
            return;
        }
        CancelPayOrderContext context = new CancelPayOrderContext();
        context.setOrderId(tradeOrderDO.getOrderId());
        context.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        PayMethodHandler payMethodHandler = PayMethodHandlerFactory.
                getPayMethodHandler(tradeOrderDO.getPayMethod());
        payMethodHandler.cancelPayOrder(context);


    }


}