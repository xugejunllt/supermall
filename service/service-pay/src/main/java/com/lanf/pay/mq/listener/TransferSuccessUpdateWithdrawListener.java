package com.lanf.pay.mq.listener;


import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.constant.TransferEventTagConstant;
import com.lanf.api.pay.mq.message.TransferSuccessMessage;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.entity.WalletAccountDO;
import com.lanf.pay.model.entity.WalletAccountFlowDO;
import com.lanf.pay.model.entity.WalletWithdrawDO;
import com.lanf.pay.model.enums.WalletEventTypeEnum;
import com.lanf.api.pay.model.enums.WithdrawStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.wallet.IWalletAccountFlowService;
import com.lanf.pay.service.wallet.IWalletAccountService;
import com.lanf.pay.service.wallet.IWalletWithdrawService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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

    @Autowired
    private IWalletAccountFlowService walletAccountFlowService;

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
        if (WithdrawStatusEnum.SUCCESS.equals(withdraw.getStatus())
                 || WithdrawStatusEnum.FAILED.equals(withdraw.getStatus())
           || WithdrawStatusEnum.CANCELLED.equals(withdraw.getStatus())) {
            log.info("提现已完成");
            return;
        }
        if (!WithdrawStatusEnum.PROCESSING.equals(withdraw.getStatus())) {
            log.error("提现单状态不是处理中，当前状态: {}, ID: {}", withdraw.getStatus(), withdrawId);
            return;
        }
        WalletAccountDO accountDO = walletAccountService.getById(withdraw.getWalletAccountId());
        if (accountDO == null) {
            log.error("钱包账户不存在，ID: {}", withdraw.getWalletAccountId());
            return;
        }

        Boolean result = message.getResult();

        if (result){

            WalletAccountFlowDO walletAccountFlowDO = buildWalletAccountFlowDO(withdraw,transAmount,accountDO);
            BigDecimal currentFrozenBalance = accountDO.getFrozenBalance();
            //扣除冻结金额
            BigDecimal afterFrozenBalance = BigDecimalUtils.subtract(currentFrozenBalance, transAmount);
            try {
                walletAccountFlowService.save(walletAccountFlowDO);
            } catch (DuplicateKeyException e) {
                log.warn("钱包账户流水记录已存在");
                return;
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
            boolean updated = walletWithdrawService.lambdaUpdate()
                    .eq(WalletWithdrawDO::getId, withdrawId)
                    .eq(WalletWithdrawDO::getStatus, WithdrawStatusEnum.PROCESSING)
                    .eq(WalletWithdrawDO::getVersion, withdraw.getVersion())
                    .set(WalletWithdrawDO::getStatus, WithdrawStatusEnum.SUCCESS)
                    .set(WalletWithdrawDO::getVersion, withdraw.getVersion() + 1)
                    .update();
            if (!updated) {
                log.warn("更新提现单状态失败，可能已被处理，ID: {}", withdrawId);
                throw new MessageRetryConsumeException("更新提现单状态失败");
            }

        } else {

            //1.回加解冻金额到可用余额
            BigDecimal currentFrozenBalance = accountDO.getFrozenBalance();
            BigDecimal afterFrozenBalance = BigDecimalUtils.subtract(currentFrozenBalance, transAmount);
            BigDecimal currentBalance =  BigDecimalUtils.add(accountDO.getBalance(), transAmount);

            boolean accountUpdated = walletAccountService.lambdaUpdate()
                    .eq(WalletAccountDO::getId, accountDO.getId())
                    .eq(WalletAccountDO::getVersion, accountDO.getVersion())
                    .set(WalletAccountDO::getFrozenBalance, afterFrozenBalance)
                    .set(WalletAccountDO::getBalance, currentBalance)
                    .set(WalletAccountDO::getVersion, accountDO.getVersion() + 1)
                    .update();
            if (!accountUpdated) {
                log.warn("更新钱包账户冻结余额失败，账户ID: {}", accountDO.getId());
                throw new MessageRetryConsumeException("更新钱包账户冻结余额失败");
            }
            boolean updated = walletWithdrawService.lambdaUpdate()
                    .eq(WalletWithdrawDO::getId, withdrawId)
                    .eq(WalletWithdrawDO::getStatus, WithdrawStatusEnum.PROCESSING)
                    .eq(WalletWithdrawDO::getVersion, withdraw.getVersion())
                    .set(WalletWithdrawDO::getStatus, WithdrawStatusEnum.FAILED)
                    .set(WalletWithdrawDO::getVersion, withdraw.getVersion() + 1)
                    .update();
            if (!updated) {
                log.warn("更新提现单状态失败，可能已被处理，ID: {}", withdrawId);
                throw new MessageRetryConsumeException("更新提现单状态失败");
            }
        }



        log.info("提现处理完成，提现单ID: {}, 用户ID: {}, 金额: {}", withdrawId, withdraw.getUserId(), transAmount);

    }

    private WalletAccountFlowDO buildWalletAccountFlowDO(WalletWithdrawDO withdraw,
                                                         BigDecimal transAmount,WalletAccountDO accountDO) {
        BigDecimal beforeBalance = BigDecimalUtils.add(accountDO.getBalance(),
                accountDO.getFrozenBalance());
        BigDecimal afterBalance = BigDecimalUtils.subtract(beforeBalance, transAmount);
        WalletAccountFlowDO flowDO = new WalletAccountFlowDO();
        flowDO.setUserId(withdraw.getUserId());
        flowDO.setFlowNo(withdraw.getWithdrawNo());
        flowDO.setWalletAccountId(accountDO.getId());
        flowDO.setBeforeBalance(accountDO.getBalance());
        flowDO.setAfterBalance(afterBalance);
        flowDO.setChangeBalance(transAmount);
        flowDO.setBizOrderId(withdraw.getId());
        flowDO.setEventType(WalletEventTypeEnum.WITHDRAW);

        return flowDO;
    }

}
