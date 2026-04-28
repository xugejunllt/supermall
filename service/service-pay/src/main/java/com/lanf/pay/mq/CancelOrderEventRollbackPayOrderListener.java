package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.CancelPayOrderContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.trade.PayMethodHandler;
import com.lanf.pay.service.trade.PayMethodHandlerFactory;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 分发多渠道支付方式
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.CANCEL_ORDER_EVENT_TOPIC, consumerGroup = TopicName.CANCEL_ORDER_EVENT_PAY_GROUP)
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
    public void onMessage(CancelOrderEventMessage message)  {
        log.info("取消订单事件回滚三方支付订单开始:[{{}}]", JsonUtils.toJsonString(message));

        Long orderId = message.getOrderId();
        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOrderId, orderId).one();
        if (tradeOrderDO == null) {
            log.error("交易单不存在");
            return;
        }
        CancelPayOrderContext context = new CancelPayOrderContext();
        context.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        context.setTradeOrderId(tradeOrderDO.getId());
        PayMethodHandler payMethodHandler = PayMethodHandlerFactory.getPayMethodHandler(tradeOrderDO.getPayMethod());
        payMethodHandler.cancelPayOrder(context);



    }






}