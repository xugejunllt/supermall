package com.lanf.pay.mq.listener;

import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.message.WalletRechargeMessage;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.pay.constant.PayMqGroupName;
import com.lanf.pay.model.entity.WalletAccountDO;
import com.lanf.pay.model.entity.WalletAccountFlowDO;
import com.lanf.pay.model.enums.WalletEventTypeEnum;
import com.lanf.pay.service.wallet.IWalletAccountFlowService;
import com.lanf.pay.service.wallet.IWalletAccountService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 钱包充值
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

    @Autowired
    private IWalletAccountFlowService walletAccountFlowService;


    @Transactional
    @Override
    public void onMessage(WalletRechargeMessage message) {

        log.info("钱包充值");
        BigDecimal amount = message.getAmount();
        Long userId = message.getUserId();
        String flowNo = message.getFlowNo();
        Long bizOrderId = message.getBizOrderId();

        WalletAccountDO one = walletAccountService.lambdaQuery().eq(WalletAccountDO::getUserId, userId).one();
        if (one == null) {
            log.error("用户钱包账户不存在");
            return;

        }
        //可用余额
        BigDecimal updateBalance = BigDecimalUtils.add(one.getBalance(), amount);

        WalletAccountFlowDO walletAccountFlowDO = new WalletAccountFlowDO();
        walletAccountFlowDO.setFlowNo(flowNo);
        walletAccountFlowDO.setUserId(userId);
        walletAccountFlowDO.setWalletAccountId(one.getId());
        walletAccountFlowDO.setBeforeBalance(one.getBalance());
        walletAccountFlowDO.setChangeBalance(amount);
        walletAccountFlowDO.setAfterBalance(updateBalance);
        walletAccountFlowDO.setBizOrderId(bizOrderId);
        walletAccountFlowDO.setEventType(WalletEventTypeEnum.RECHARGE);

        try {
            walletAccountFlowService.save(walletAccountFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("钱包充值记录已存在");
            return;
        }
        boolean update = walletAccountService.lambdaUpdate()
                .eq(WalletAccountDO::getId, one.getId())
                .eq(WalletAccountDO::getVersion, one.getVersion())
                .set(WalletAccountDO::getBalance, updateBalance)
                .set(WalletAccountDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            /**
             * 并发下 其他任务中执行 进行重试
             */
            log.warn("更新用户钱包账户失败");
            throw new MessageRetryConsumeException("更新用户钱包账户失败");
        }

    }


}
