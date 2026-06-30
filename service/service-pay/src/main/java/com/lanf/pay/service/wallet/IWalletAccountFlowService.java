package com.lanf.pay.service.wallet;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.query.WalletAccountFlowPageQuery;
import com.lanf.api.pay.model.vo.WalletAccountFlowPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.WalletAccountFlowDO;

/**
 * <p>
 * 钱包账户表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
public interface IWalletAccountFlowService extends IService<WalletAccountFlowDO> {

    /**
     * 分页查询钱包账户流水
     */
    PageResult<WalletAccountFlowPageVO> walletAccountFlowPageQuery(WalletAccountFlowPageQuery query);
}
