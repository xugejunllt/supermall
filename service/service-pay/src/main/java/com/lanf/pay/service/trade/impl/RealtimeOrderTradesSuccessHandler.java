package com.lanf.pay.service.trade.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.trade.TradeSuccessHandler;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OrderPayInfo;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class RealtimeOrderTradesSuccessHandler implements TradeSuccessHandler {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Override
    public void postTradeSuccessHandler(PostTradeSuccessHandlerContext context) {
        log.info("交易成功后置处理:{}",context);
        TradeOrderDO tradeOrderDO = context.getTradeOrderDO();
        TradeSuccessEventMessage message = buildTradeSuccessEventMessage(tradeOrderDO);
        rocketMqClient.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC, JsonUtils.toJsonString(message));
    }

    private TradeSuccessEventMessage buildTradeSuccessEventMessage( TradeOrderDO tradeOrderDO){
        List<OrderPayInfo> orderPayInfoList = new ArrayList<>();
        OrderPayInfo orderPayInfo = new OrderPayInfo();
        orderPayInfo.setOrderId(tradeOrderDO.getOrderId());
        orderPayInfoList.add(orderPayInfo);
        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay( false);
        message.setUserId(tradeOrderDO.getUserId());
        message.setOrderPayInfoList(orderPayInfoList);
        return message;
    }

}
