package com.lanf.pay.service.trade.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.trade.TradeSuccessHandler;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OrderPayInfo;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RealtimeOrderTradesSuccessHandler implements TradeSuccessHandler {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Override
    public void postTradeSuccessHandler(PostTradeSuccessHandlerContext context) {

        TradeOrderDO tradeOrderDO = context.getTradeOrderDO();
        Integer payType = tradeOrderDO.getPayType().getCode();
        TradeSuccessEventMessage message = buildTradeSuccessEventMessage(payType,tradeOrderDO);
        rocketMqClient.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC, JsonUtils.toJsonString(message));
    }

    private TradeSuccessEventMessage buildTradeSuccessEventMessage(Integer payType, TradeOrderDO tradeOrderDO){
        List<OrderPayInfo> orderPayInfoList = new ArrayList<>();
        OrderPayInfo orderPayInfo = new OrderPayInfo();
        orderPayInfo.setOrderId(tradeOrderDO.getOrderId());
        orderPayInfo.setPayMoney(tradeOrderDO.getTradeMoney());
        orderPayInfo.setPayType(payType);
        orderPayInfo.setMerchantId(tradeOrderDO.getBusinessId());
        orderPayInfoList.add(orderPayInfo);
        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay( false);
        message.setMainOrderId(tradeOrderDO.getOrderId());
        message.setOrderPayInfoList(orderPayInfoList);
        return message;
    }

}
