package com.lanf.pay.service.wallet;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.bo.AddWalletAccount;
import com.lanf.pay.model.dto.BalanceOrderDTO;
import com.lanf.pay.model.dto.WithdrawApplyDTO;
import com.lanf.pay.model.entity.WalletAccountDO;

/**
 * <p>
 * 钱包账户表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
public interface IWalletAccountService extends IService<WalletAccountDO> {

    /**
     * 添加钱包账号
     *
     */
    void addWalletAccount(AddWalletAccount dto);

    /**
     * 钱包余额下单
     *
     */
    void balanceOrder(BalanceOrderDTO dto);

    /**
     *
     * 钱包下单时 订单取消 回滚钱包金额
     *
     */
    void rollbackWalletBalanceOnCancelOrder(Long bizOrderId);

    /**
     * 申请提现
     *
     * @param dto 提现申请信息
     */
    void applyWithdraw(WithdrawApplyDTO dto);

    /**
     * 同意提现并发送MQ消息
     *
     * @param withdrawId 提现单ID
     */
    void approveWithdraw(Long withdrawId);
}
