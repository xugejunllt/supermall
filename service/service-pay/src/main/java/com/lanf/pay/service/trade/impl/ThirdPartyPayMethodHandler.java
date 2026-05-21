package com.lanf.pay.service.trade.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.CancelPayOrderContext;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.trade.PayMethodHandler;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class ThirdPartyPayMethodHandler implements PayMethodHandler {

    @Autowired
    private IPrepayPayTypeService prepayPayTypeService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Override
    public void cancelPayOrder(CancelPayOrderContext context) {

        log.info("取消三方支付订单,查询多渠道支付方式:{}",context);

        String outTradeNo = context.getOutTradeNo();
        List<Integer> payTypesByOutTradeNo = prepayPayTypeService.getPayTypesByOutTradeNo(outTradeNo);
        if (payTypesByOutTradeNo.isEmpty()) {
            log.info("未查询到支付方式");
            return;
        }

        for (Integer payType : payTypesByOutTradeNo){

            CancelOrderMessage cancelOrderMessage = new CancelOrderMessage();
            cancelOrderMessage.setOutTradeNo(outTradeNo);
            cancelOrderMessage.setPayType(payType);
            cancelOrderMessage.setBizOrderId(context.getTradeOrderId());
            rocketMqClient.sendMessage(TopicName.CANCEL_PAY_ORDER_TOPIC, JsonUtils.toJsonString(cancelOrderMessage));

        }
    }
}
