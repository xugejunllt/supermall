package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.pay.service.trade.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.model.message.CancelOrderMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
    @Transactional
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
        String outTradeNo = tradeOrderDO.getOutTradeNo();
        List<Integer> payTypesByOutTradeNo = prepayPayTypeService.getPayTypesByOutTradeNo(outTradeNo);
        if (payTypesByOutTradeNo.isEmpty()) {
            log.info("未查询到支付方式");
           return;
        }

        for (Integer payType : payTypesByOutTradeNo){

            CancelOrderMessage cancelOrderMessage = new CancelOrderMessage();
            cancelOrderMessage.setOutTradeNo(outTradeNo);
            cancelOrderMessage.setPayType(payType);
            cancelOrderMessage.setCancelSource(message.getCancelSource());
            //取消订单 全部退款时 outRequestNo = outTradeNo
            cancelOrderMessage.setOutRequestNo(outTradeNo);
            rocketMqClient.sendMessage(TopicName.CANCEL_PAY_ORDER_TOPIC, JsonUtils.toJsonString(cancelOrderMessage));

        }



    }






}