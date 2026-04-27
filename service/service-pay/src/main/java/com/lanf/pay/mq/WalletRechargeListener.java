package com.lanf.pay.mq;

import com.lanf.client.pay.mq.PayClientTopicName;
import com.lanf.client.pay.mq.message.WalletRechargeMessage;
import com.lanf.pay.constant.PayMqGroupName;
import com.lanf.pay.service.wallet.IWalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 钱包充值
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = PayClientTopicName.WALLET_RECHARGE_TOPIC,
    consumerGroup = PayMqGroupName.WALLET_RECHARGE_GROUP
)
public class WalletRechargeListener implements RocketMQListener<WalletRechargeMessage> {

    @Autowired
    private IWalletAccountService walletAccountService;

    @Override
    public void onMessage(WalletRechargeMessage message) {

       log.info("钱包充值");



    }
}
