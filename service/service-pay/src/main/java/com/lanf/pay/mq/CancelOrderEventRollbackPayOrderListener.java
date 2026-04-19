package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.CancelTradeOrderTradeStatusBO;
import com.lanf.pay.model.bo.TradeOrderPayStatusBO;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.TradeStatusEnum;
import com.lanf.pay.model.vo.OutTradeNoAndPayType;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 取消三方订单
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
    @Transactional
    @Override
    public void onMessage(CancelOrderEventMessage message) {
        log.info("取消订单事件回滚三方支付订单开始:[{{}}]", JsonUtils.toJsonString(message));

        Long orderId = message.getOrderId();
        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery().eq(TradeOrderDO::getOrderId, orderId).one();
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
        TradeOrderPayStatusBO tradeOrderPayStatusBO = queryTradeOrderPayStatus(outTradeNo, payTypesByOutTradeNo);
        List<OutTradeNoAndPayType> waitPayList = tradeOrderPayStatusBO.getWaitPayList();
        List<OutTradeNoAndPayType> successPayList = tradeOrderPayStatusBO.getSuccessPayList();
        if (!waitPayList.isEmpty()){
            for (OutTradeNoAndPayType payType : successPayList) {
                Integer payType1 = payType.getPayType();
                String outTradeNo1 = payType.getOutTradeNo();
                PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType1);

                boolean cancelled = paymentService.cancelPendingOrder(outTradeNo1);
                if (cancelled) {
                    log.info("取消支付订单成功");
                } else {
                    log.warn("取消支付订单失败");
                }
            }
        }


    }
    private TradeOrderPayStatusBO queryTradeOrderPayStatus(String outTradeNo, List<Integer> payTypes) {

        List<CancelTradeOrderTradeStatusBO> tradeStatusBOList = new ArrayList<>();
        for (Integer payType : payTypes) {

            PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);

            TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);
            CancelTradeOrderTradeStatusBO cancelThirdPartyPaymentsBO = new CancelTradeOrderTradeStatusBO();
            cancelThirdPartyPaymentsBO.setPayType(payType);
            cancelThirdPartyPaymentsBO.setOutTradeNo(outTradeNo);
            cancelThirdPartyPaymentsBO.setTradeStatus(tradeStatusBO.getTradeStatus());
            tradeStatusBOList.add(cancelThirdPartyPaymentsBO);
        }
        /**
         * 三方交易单状态 WAIT_BUYER_PAY WAIT_BUYER_PAY
         * 状态下才允许被取消
         *
         */
        List<CancelTradeOrderTradeStatusBO> notExistTradeStatusBOList = tradeStatusBOList.stream().filter(cancelThirdPartyPaymentsBO ->
                TradeStatusEnum.NOT_EXIST.
                        equals(cancelThirdPartyPaymentsBO.getTradeStatus())).collect(Collectors.toList());
        if (notExistTradeStatusBOList.size() == tradeStatusBOList.size()) {
            log.info("所有支付渠道交易单不存在");

            return new TradeOrderPayStatusBO();
        }

        List<CancelTradeOrderTradeStatusBO> waitPayTradeStatusBOList = tradeStatusBOList.stream().filter(cancelThirdPartyPaymentsBO ->
                TradeStatusEnum.WAIT_BUYER_PAY.
                        equals(cancelThirdPartyPaymentsBO.getTradeStatus())).collect(Collectors.toList());
        List<OutTradeNoAndPayType> waitPayList = new ArrayList<>();
        if (!waitPayTradeStatusBOList.isEmpty()) {
            /**
             *
             * 待支付的交易单
             *
             */
            waitPayTradeStatusBOList.forEach(a -> {
                OutTradeNoAndPayType outTradeNoAndPayType = new OutTradeNoAndPayType();
                outTradeNoAndPayType.setOutTradeNo(a.getOutTradeNo());
                outTradeNoAndPayType.setPayType(a.getPayType());
                waitPayList.add(outTradeNoAndPayType);
            });

        }
        List<CancelTradeOrderTradeStatusBO> successPayTradeStatusBOList = tradeStatusBOList.stream().filter(cancelThirdPartyPaymentsBO ->
                TradeStatusEnum.TRADE_SUCCESS.
                        equals(cancelThirdPartyPaymentsBO.getTradeStatus())).collect(Collectors.toList());
        List<OutTradeNoAndPayType> successPayList = new ArrayList<>();

        if (!successPayTradeStatusBOList.isEmpty()) {
            /**
             * 支付成功的交易单
             *
             */
            successPayTradeStatusBOList.forEach(a -> {
                OutTradeNoAndPayType outTradeNoAndPayType = new OutTradeNoAndPayType();
                outTradeNoAndPayType.setOutTradeNo(a.getOutTradeNo());
                outTradeNoAndPayType.setPayType(a.getPayType());
                successPayList.add(outTradeNoAndPayType);
            });


        }
        TradeOrderPayStatusBO tradeOrderPayStatusBO = new TradeOrderPayStatusBO();
        tradeOrderPayStatusBO.setSuccessPayList(successPayList);
        tradeOrderPayStatusBO.setWaitPayList(waitPayList);

        return tradeOrderPayStatusBO;
    }





}