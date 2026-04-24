package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.CancelPaidOrderBO;
import com.lanf.pay.model.bo.CancelWaitPayOrderBO;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.enums.TradeStatusEnum;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderMessage;
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
        topic = TopicName.CANCEL_PAY_ORDER_TOPIC,
        consumerGroup = TopicName.CANCEL_PAY_ORDER_TOPIC_GROUP
)
public class CancelPayOrderListener implements RocketMQListener<CancelOrderMessage> {

    @Autowired
    private IPaymentCancelRecordService paymentCancelRecordService;

    @Override
    public void onMessage(CancelOrderMessage message) {

        log.info("取消待支付订单开始:[{{}}]", JsonUtils.toJsonString(message));

        Integer payType = message.getPayType();
        String outTradeNo = message.getOutTradeNo();
        /**
         * 查询三方交易单状态
         */
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);

        TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);
        TradeStatusEnum tradeStatus = tradeStatusBO.getTradeStatus();

        switch (tradeStatus) {
            case UNKNOWN:
                log.error("未知订单状态");
                return;
            case NOT_EXIST:
                log.info("交易不存在");
                return;

            case WAIT_BUYER_PAY:
                log.info("取消待支付订单");
                CancelWaitPayOrderBO cancelWaitPayOrderBO = new CancelWaitPayOrderBO();
                cancelWaitPayOrderBO.setOutTradeNo(message.getOutTradeNo());
                cancelWaitPayOrderBO.setPayType(message.getPayType());
                cancelWaitPayOrderBO.setCancelSource(message.getCancelSource());
                cancelWaitPayOrderBO.setCurrentPayStatus(TradeStatusEnum.WAIT_BUYER_PAY.getCode());
                paymentCancelRecordService.cancelWaitPayOrder(cancelWaitPayOrderBO);
                return;
            case TRADE_CLOSED:
                log.info("订单已关闭");
                return;
            case TRADE_SUCCESS:
                log.info("取消已支付的订单");
                CancelPaidOrderBO cancelPaidOrderBO = new CancelPaidOrderBO();
                cancelPaidOrderBO.setOutTradeNo(message.getOutTradeNo());
                cancelPaidOrderBO.setPayType(message.getPayType());
                cancelPaidOrderBO.setCancelSource(message.getCancelSource());
                cancelPaidOrderBO.setCurrentPayStatus(TradeStatusEnum.TRADE_SUCCESS.getCode());
                cancelPaidOrderBO.setOutRequestNo(tradeStatusBO.getTradeNo());
                paymentCancelRecordService.cancelPaidOrder(cancelPaidOrderBO);
                return;
            case TRADE_FINISHED:
                log.info("支付订单已完成");

        }




    }
}
