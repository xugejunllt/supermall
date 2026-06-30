package com.lanf.pay.service.wallet;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.WalletWithdrawDO;
import com.lanf.api.pay.model.query.WalletWithdrawPageQuery;
import com.lanf.api.pay.model.vo.WalletWithdrawPageVO;

/**
 * <p>
 * 钱包提现记录表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-28
 */
public interface IWalletWithdrawService extends IService<WalletWithdrawDO> {

    /**
     * 分页查询钱包提现记录
     */
    PageResult<WalletWithdrawPageVO> walletWithdrawPageQuery(WalletWithdrawPageQuery query);
}
