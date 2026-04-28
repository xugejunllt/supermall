package com.lanf.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.finance.model.bo.AddMoneyFlow;
import com.lanf.finance.model.entity.MoneyFlowDO;

/**
 * <p>
 * 资金流水 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
public interface IMoneyFlowService extends IService<MoneyFlowDO> {

    /**
     * 添加资金流水
     *
     */
    void addMoneyFlow(AddMoneyFlow addMoneyFlow);

}
