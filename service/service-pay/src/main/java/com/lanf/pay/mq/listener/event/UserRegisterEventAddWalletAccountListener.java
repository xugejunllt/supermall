package com.lanf.pay.mq.listener.event;

import com.lanf.api.user.mq.UserClientTopicName;
import com.lanf.api.user.mq.message.UserRegisterMessage;
import com.lanf.pay.constant.PayMqGroupName;
import com.lanf.pay.model.bo.AddWalletAccount;
import com.lanf.pay.service.wallet.IWalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册成功--创建用户钱包
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = UserClientTopicName.USER_REGISTER_EVENT_TOPIC,
    consumerGroup = PayMqGroupName.USER_REGISTER_EVENT_PAY_GROUP
)
public class UserRegisterEventAddWalletAccountListener implements RocketMQListener<UserRegisterMessage> {

    @Autowired
    private IWalletAccountService walletAccountService;

    @Override
    public void onMessage(UserRegisterMessage message) {

       log.info("支付服务，监听用户注册事件");
        AddWalletAccount dto = new AddWalletAccount();
        dto.setUserId(message.getUserId());
        walletAccountService.addWalletAccount(dto);


    }
}
