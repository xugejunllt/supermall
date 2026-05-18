package com.lanf.pay.service.wallet.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.PayMethodEnum;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.PayOrderFlowInsertSuccessMessage;
import com.lanf.api.pay.mq.message.TransferMessage;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.pay.mapper.WalletAccountMapper;
import com.lanf.pay.model.bo.AddWalletAccount;
import com.lanf.pay.model.dto.BalanceOrderDTO;
import com.lanf.pay.model.dto.WithdrawApplyDTO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.entity.WalletAccountDO;
import com.lanf.pay.model.entity.WalletAccountFlowDO;
import com.lanf.pay.model.entity.WalletWithdrawDO;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.model.enums.WalletEventTypeEnum;
import com.lanf.pay.model.enums.WithdrawStatusEnum;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.wallet.IWalletAccountFlowService;
import com.lanf.pay.service.wallet.IWalletAccountService;
import com.lanf.pay.service.wallet.IWalletWithdrawService;
import com.lanf.pay.utils.PayServiceUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class WalletAccountServiceImpl extends ServiceImpl<WalletAccountMapper, WalletAccountDO> implements IWalletAccountService {

    @Lazy
    @Autowired
    private ITradeOrderService tradeOrderService;

    @Autowired
    private IWalletAccountFlowService walletAccountFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IWalletWithdrawService walletWithdrawService;

    @Override
    public void addWalletAccount(AddWalletAccount dto) {

        WalletAccountDO walletAccountDO = new WalletAccountDO();
        walletAccountDO.setUserId(dto.getUserId());
        try {
            this.save(walletAccountDO);
        } catch (DuplicateKeyException e) {
            log.warn("该用户已存在钱包账户");
        }


    }

    @Transactional
    @Override
    public void balanceOrder(BalanceOrderDTO dto) {

        Long userId = UserContext.getUserId();
        String orderNumber = dto.getOrderNumber();
        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOrderNumber, orderNumber)
                .one();
        if (tradeOrderDO == null) {
            log.error("该订单不存在");
            throw new BizException("该订单不存在");
        }
        if (!TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrderDO.getPayStatus())) {
            log.warn("订单状态异常");
            throw new BizException("订单状态异常");
        }

        BigDecimal tradeMoney = tradeOrderDO.getTradeMoney();

        WalletAccountDO accountDO = this.lambdaQuery().eq(WalletAccountDO::getUserId, userId).one();
        if (accountDO == null) {
            log.error("用户钱包账户不存在");
            throw new BizException("用户钱包账户不存在");
        }
        BigDecimal balance = accountDO.getBalance();
        if (BigDecimalUtils.compareTo(balance, tradeMoney) == -1) {
            log.warn("余额不足");
            throw new BizException("余额不足");
        }
        BigDecimal afterBalance = balance.subtract(tradeMoney);

        WalletAccountFlowDO walletAccountFlowDO = new WalletAccountFlowDO();
        walletAccountFlowDO.setUserId(userId);
        walletAccountFlowDO.setFlowNo(PayServiceUtils.generateOutTradeNo(orderNumber));
        walletAccountFlowDO.setWalletAccountId(accountDO.getId());
        walletAccountFlowDO.setBeforeBalance(balance);
        walletAccountFlowDO.setAfterBalance(afterBalance);
        walletAccountFlowDO.setChangeBalance(tradeMoney);
        walletAccountFlowDO.setBizOrderId(tradeOrderDO.getId());
        walletAccountFlowDO.setEventType(WalletEventTypeEnum.ORDER);

        try {
            walletAccountFlowService.save(walletAccountFlowDO);
        } catch (Exception e) {
            log.warn("钱包账户流水记录已存在");
            return;
        }
        boolean update = this.lambdaUpdate()
                .eq(WalletAccountDO::getId, accountDO.getId())
                .eq(WalletAccountDO::getVersion, accountDO.getVersion())
                .set(WalletAccountDO::getBalance, afterBalance)
                .set(WalletAccountDO::getVersion, accountDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新用户钱包账户失败");
            throw new BizException("更新用户钱包账户失败");
        }

        PayOrderFlowInsertSuccessMessage message = buildPayOrderFlowInsertSuccessMessage(tradeOrderDO, tradeMoney);
        rocketMqClient.sendMessage(PayClientTopicName.PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC, JsonUtils.toJsonString(message));
    }


    private static PayOrderFlowInsertSuccessMessage buildPayOrderFlowInsertSuccessMessage(TradeOrderDO tradeOrderDO, BigDecimal tradeMoney) {
        PayOrderFlowInsertSuccessMessage message = new PayOrderFlowInsertSuccessMessage();
        message.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        message.setBathPay(false);
        message.setPayType(null);
        message.setTradePurpose(TradePurposeEnum.REALTIME_ORDER);
        message.setPayMethod(PayMethodEnum.WALLET_BALANCE);
        return message;
    }

    @Override
    public void rollbackWalletBalanceOnCancelOrder(Long bizOrderId) {

        WalletAccountFlowDO accountFlowDO = walletAccountFlowService.lambdaQuery()
                .eq(WalletAccountFlowDO::getBizOrderId, bizOrderId)
                .eq(WalletAccountFlowDO::getEventType, WalletEventTypeEnum.ORDER)
                .one();
        if (accountFlowDO == null) {
            log.error("该订单不存在钱包账户流水");
            return;
        }
        Long walletAccountId = accountFlowDO.getWalletAccountId();
        WalletAccountDO accountDO = this.getById(walletAccountId);
        if (accountDO == null) {
            log.error("用户钱包账户不存在");
            return;
        }

        BigDecimal changeBalance = accountFlowDO.getChangeBalance();
        BigDecimal currentBalance = accountDO.getBalance();
        BigDecimal afterBalance = BigDecimalUtils.add(currentBalance, changeBalance);

        WalletAccountFlowDO rollbackFlowDO = new WalletAccountFlowDO();
        rollbackFlowDO.setUserId(accountDO.getUserId());
        rollbackFlowDO.setFlowNo(PayServiceUtils.generateOutTradeNo(bizOrderId.toString() + "_rollback"));
        rollbackFlowDO.setWalletAccountId(walletAccountId);
        rollbackFlowDO.setBeforeBalance(currentBalance);
        rollbackFlowDO.setAfterBalance(afterBalance);
        rollbackFlowDO.setChangeBalance(changeBalance);
        rollbackFlowDO.setBizOrderId(bizOrderId);
        rollbackFlowDO.setEventType(WalletEventTypeEnum.CANCEL_ORDER_ROLLBACK);

        try {
            walletAccountFlowService.save(rollbackFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("取消订单回滚流水记录已存在");
            return;
        }

        boolean update = this.lambdaUpdate()
                .eq(WalletAccountDO::getId, walletAccountId)
                .eq(WalletAccountDO::getVersion, accountDO.getVersion())
                .set(WalletAccountDO::getBalance, afterBalance)
                .set(WalletAccountDO::getVersion, accountDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("回滚用户钱包账户失败");
            throw new BizException("回滚用户钱包账户失败");
        }

        log.info("订单 {} 取消成功，钱包余额回滚完成", bizOrderId);
    }

    @Transactional
    @Override
    public void applyWithdraw(WithdrawApplyDTO dto) {

        Long userId = UserContext.getUserId();
        WalletAccountDO accountDO = this.lambdaQuery()
                .eq(WalletAccountDO::getUserId, userId)
                .one();
        if (accountDO == null) {
            log.error("用户钱包账户不存在");
            throw new BizException("用户钱包账户不存在");
        }

        BigDecimal balance = accountDO.getBalance();
        BigDecimal withdrawAmount = dto.getAmount();
        
        if (BigDecimalUtils.compareTo(balance, withdrawAmount) < 0) {
            log.warn("用户钱包余额不足，当前余额: {}, 提现金额: {}", balance, withdrawAmount);
            throw new BizException("用户钱包余额不足");
        }

        BigDecimal frozenBalance = BigDecimalUtils.add(accountDO.getFrozenBalance(), withdrawAmount);
        BigDecimal afterBalance = BigDecimalUtils.subtract(balance, withdrawAmount);
        WalletWithdrawDO walletWithdrawDO = buildWalletWithdrawDO(dto, accountDO);

        /**
         * 冻结钱金额
         */
        boolean update = this.lambdaUpdate()
                .eq(WalletAccountDO::getId, accountDO.getId())
                .eq(WalletAccountDO::getVersion, accountDO.getVersion())
                .set(WalletAccountDO::getBalance, afterBalance)
                .set(WalletAccountDO::getFrozenBalance, frozenBalance)
                .set(WalletAccountDO::getVersion, accountDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新用户钱包账户失败");
            throw new BizException("更新用户钱包账户失败");
        }

        walletWithdrawService.save(walletWithdrawDO);


    }


    private WalletWithdrawDO buildWalletWithdrawDO(WithdrawApplyDTO dto, WalletAccountDO accountDO){

        Long withdrawId = IdUtils.generateId();
        String withdrawNo = CodeGenerateUtils.generateFlowNo(
                FlowNoPrefixEnum.WALLET_FLOW , withdrawId.toString());
        WalletWithdrawDO withdraw = new WalletWithdrawDO();
        withdraw.setId(withdrawId);
        withdraw.setUserId(UserContext.getUserId());
        withdraw.setWalletAccountId(accountDO.getId());
        withdraw.setWithdrawNo(withdrawNo);
        withdraw.setAmount(dto.getAmount());
        withdraw.setWithdrawType(dto.getWithdrawType());
        withdraw.setPayeeAccount(dto.getPayeeAccount());
        withdraw.setStatus(WithdrawStatusEnum.PENDING);
        withdraw.setRemark(dto.getRemark());
        withdraw.setVersion(0L);

        return withdraw;
    }

    @Transactional
    @Override
    public void approveWithdraw(Long withdrawId) {
        log.info("开始处理同意提现，提现单ID: {}", withdrawId);

        WalletWithdrawDO withdraw = walletWithdrawService.getById(withdrawId);
        if (withdraw == null) {
            log.error("提现单不存在，ID: {}", withdrawId);
            throw new BizException("提现单不存在");
        }

        if (!WithdrawStatusEnum.PENDING.equals(withdraw.getStatus())) {
            log.warn("提现单状态不是待处理，当前状态: {}, ID: {}", withdraw.getStatus(), withdrawId);
            throw new BizException("提现单状态不正确");
        }

        boolean updated = walletWithdrawService.lambdaUpdate()
                .eq(WalletWithdrawDO::getId, withdrawId)
                .eq(WalletWithdrawDO::getStatus, WithdrawStatusEnum.PENDING.getCode())
                .set(WalletWithdrawDO::getStatus, WithdrawStatusEnum.PROCESSING.getCode())
                .update();

        if (!updated) {
            log.error("更新提现单状态失败，ID: {}", withdrawId);
            throw new BizException("更新提现单状态失败");
        }

        TransferMessage transferMessage = buildTransferMessage(withdraw);
        rocketMqClient.sendMessage(PayClientTopicName.TRANSFER_TOPIC, JsonUtils.toJsonString(transferMessage));

        log.info("同意提现成功，已发送转账消息，提现单ID: {}, 提现单号: {}", withdrawId, withdraw.getWithdrawNo());
    }

    private TransferMessage buildTransferMessage(WalletWithdrawDO withdraw) {

        TransferMessage message = new TransferMessage();
        message.setOutBizNo(withdraw.getWithdrawNo());
        message.setUserId(withdraw.getUserId());
        message.setMerchantId(null);
        message.setBizOrderId(withdraw.getId());
        message.setEventType(TransferEventTypeEnum.WALLET_WITHDRAW);
        message.setTransferChannel(convertWithdrawTypeToPayType(withdraw.getWithdrawType()));
        /**
         * 待添加查询方法
         */
        message.setFromAccount(null);
        message.setIncomeAccount(withdraw.getPayeeAccount());
        message.setTransAmount(withdraw.getAmount());
        message.setOrderTitle("钱包提现");
        return message;
    }

    private PayChannelEnum convertWithdrawTypeToPayType(Integer withdrawType) {
        if (withdrawType == null) {
            return PayChannelEnum.ALI_PAY;
        }
        if (withdrawType == 1) {
            return PayChannelEnum.ALI_PAY;
        }
       throw new BizException("提现类型不支持");
    }

}
