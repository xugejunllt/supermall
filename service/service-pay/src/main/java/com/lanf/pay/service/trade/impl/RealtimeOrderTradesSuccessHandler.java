package com.lanf.pay.service.trade.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.trade.TradeSuccessHandler;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RealtimeOrderTradesSuccessHandler implements TradeSuccessHandler {

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;
    @Transactional
    @Override
    public void postTradeSuccessHandler(PostTradeSuccessHandlerContext context) {
        log.info("交易成功后置处理:{}",context);
        TradeOrderDO tradeOrderDO = context.getTradeOrderDO();
        TradeSuccessEventMessage message = buildTradeSuccessEventMessage(tradeOrderDO);
        message.setPayType(context.getPayType());
        mqSendMessageUtils.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC, JsonUtils.toJsonString(message),null);
    }

    private TradeSuccessEventMessage buildTradeSuccessEventMessage( TradeOrderDO tradeOrderDO){

        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay( false);
        message.setUserId(tradeOrderDO.getUserId());
        message.setOrderId(tradeOrderDO.getOrderId());
        return message;
    }

}
