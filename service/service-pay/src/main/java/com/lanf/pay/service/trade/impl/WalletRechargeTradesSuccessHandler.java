package com.lanf.pay.service.trade.impl;

import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.message.WalletRechargeMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.trade.TradeSuccessHandler;
import com.lanf.pay.utils.PayServiceUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 钱包充值成功处理
 */
@Service
public class WalletRechargeTradesSuccessHandler implements TradeSuccessHandler {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Override
    public void postTradeSuccessHandler(PostTradeSuccessHandlerContext context) {

        TradeOrderDO tradeOrderDO = context.getTradeOrderDO();
        WalletRechargeMessage walletRechargeMessage = buildWalletRechargeMessage(tradeOrderDO);
        rocketMqClient.sendMessage(PayClientTopicName.WALLET_RECHARGE_TOPIC, JsonUtils.toJsonString(walletRechargeMessage));

    }

    private WalletRechargeMessage buildWalletRechargeMessage( TradeOrderDO tradeOrderDO){

        WalletRechargeMessage walletRechargeMessage = new WalletRechargeMessage();
        walletRechargeMessage.setUserId(tradeOrderDO.getUserId());
        walletRechargeMessage.setFlowNo(PayServiceUtils.generateOutTradeNo(tradeOrderDO.getOrderNumber()));
        /**
         * 充值金额 = 交易金额
         */
        walletRechargeMessage.setAmount(tradeOrderDO.getTradeMoney());
        walletRechargeMessage.setBizOrderId(tradeOrderDO.getId());
        z
        return walletRechargeMessage;

    }
}
