package com.lanf.pay.mq.listener;

import com.lanf.client.pay.model.enums.TransferEventTypeEnum;
import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.constant.TransferEventTagConstant;
import com.lanf.client.pay.mq.message.TransferSuccessMessage;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.entity.WalletAccountDO;
import com.lanf.pay.model.entity.WalletWithdrawDO;
import com.lanf.pay.model.enums.WithdrawStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.wallet.IWalletAccountService;
import com.lanf.pay.service.wallet.IWalletWithdrawService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.TRANSFER_SUCCESS_EVENT_TOPIC,
        consumerGroup = PayMqGroupName.TRANSFER_SUCCESS_WALLET_WITHDRAW_GROUP,
        selectorExpression = TransferEventTagConstant.WALLET_WITHDRAW
)
public class TransferSuccessUpdateWithdrawListener implements RocketMQListener<TransferSuccessMessage> {

    @Autowired
    private IWalletWithdrawService walletWithdrawService;

    @Autowired
    private IWalletAccountService walletAccountService;

    @Transactional
    @Override
    public void onMessage(TransferSuccessMessage message) {
        log.info("收到转账成功消息: {}", JsonUtils.toJsonString(message));

        if (!TransferEventTypeEnum.WALLET_WITHDRAW.equals(message.getEventType())) {
            log.warn("事件类型不匹配，期望: WALLET_WITHDRAW, 实际: {}", message.getEventType());
            return;
        }

        Long withdrawId = message.getBizOrderId();
        BigDecimal transAmount = message.getTransAmount();

        WalletWithdrawDO withdraw = walletWithdrawService.getById(withdrawId);
        if (withdraw == null) {
            log.error("提现单不存在，ID: {}", withdrawId);
            return;
        }
        if (WithdrawStatusEnum.SUCCESS.getCode().equals(withdraw.getStatus())) {
            log.info("提现已完成");
            return;
        }
        if (!WithdrawStatusEnum.PROCESSING.getCode().equals(withdraw.getStatus())) {
            log.error("提现单状态不是处理中，当前状态: {}, ID: {}", withdraw.getStatus(), withdrawId);
            return;
        }
        WalletAccountDO accountDO = walletAccountService.getById(withdraw.getWalletAccountId());
        if (accountDO == null) {
            log.error("钱包账户不存在，ID: {}", withdraw.getWalletAccountId());
            return;
        }
        BigDecimal currentFrozenBalance = accountDO.getFrozenBalance();
        BigDecimal afterFrozenBalance = BigDecimalUtils.subtract(currentFrozenBalance, transAmount);

        boolean updated = walletWithdrawService.lambdaUpdate()
                .eq(WalletWithdrawDO::getId, withdrawId)
                .eq(WalletWithdrawDO::getStatus, WithdrawStatusEnum.PROCESSING.getCode())
                .eq(WalletWithdrawDO::getVersion, withdraw.getVersion())
                .set(WalletWithdrawDO::getStatus, WithdrawStatusEnum.SUCCESS.getCode())
                .set(WalletWithdrawDO::getVersion, withdraw.getVersion() + 1)
                .update();

        if (!updated) {
            log.warn("更新提现单状态失败，可能已被处理，ID: {}", withdrawId);
            throw new MessageRetryConsumeException("更新提现单状态失败");

        }
        boolean accountUpdated = walletAccountService.lambdaUpdate()
                .eq(WalletAccountDO::getId, accountDO.getId())
                .eq(WalletAccountDO::getVersion, accountDO.getVersion())
                .set(WalletAccountDO::getFrozenBalance, afterFrozenBalance)
                .set(WalletAccountDO::getVersion, accountDO.getVersion() + 1)
                .update();

        if (!accountUpdated) {
            log.warn("更新钱包账户冻结余额失败，账户ID: {}", accountDO.getId());
            throw new MessageRetryConsumeException("更新钱包账户冻结余额失败");
        }
        log.info("提现处理完成，提现单ID: {}, 用户ID: {}, 金额: {}", withdrawId, withdraw.getUserId(), transAmount);

    }
}
