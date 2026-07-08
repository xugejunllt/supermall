package com.lanf.pay.service.trade.impl;

import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.WalletRechargeMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.trade.TradeSuccessHandler;
import com.lanf.pay.utils.PayServiceUtils;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 钱包充值成功处理
 */
@Slf4j
@Service
public class WalletRechargeTradesSuccessHandler implements TradeSuccessHandler {

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Override
    public void postTradeSuccessHandler(PostTradeSuccessHandlerContext context) {
        log.info("钱包充值:{}",context);
        TradeOrderDO tradeOrderDO = context.getTradeOrderDO();
        BigDecimal payMoney = context.getPayMoney();
        WalletRechargeMessage walletRechargeMessage = buildWalletRechargeMessage(tradeOrderDO,payMoney);
        mqSendMessageUtils.sendMessage(PayClientTopicName.WALLET_RECHARGE_TOPIC, JsonUtils.toJsonString(walletRechargeMessage),null);

    }

    private WalletRechargeMessage buildWalletRechargeMessage( TradeOrderDO tradeOrderDO,BigDecimal payMoney){

        WalletRechargeMessage walletRechargeMessage = new WalletRechargeMessage();
        walletRechargeMessage.setUserId(tradeOrderDO.getUserId());
        walletRechargeMessage.setFlowNo(PayServiceUtils.generateOutTradeNo(tradeOrderDO.getOrderNumber()));
        /**
         * 充值金额 = 支付回调返回的金额
         */
        walletRechargeMessage.setAmount(payMoney);
        walletRechargeMessage.setBizOrderId(tradeOrderDO.getId());

        return walletRechargeMessage;

    }
}
