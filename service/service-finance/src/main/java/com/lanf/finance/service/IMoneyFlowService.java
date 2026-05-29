package com.lanf.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.finance.model.bo.AddMoneyFlow;
import com.lanf.finance.model.entity.MoneyFlowDO;
import com.lanf.finance.model.query.SumIncomeMoneyQuery;
import com.lanf.finance.model.vo.MoneyFlowPageVO;
import com.lanf.finance.model.vo.SumIncomeMoneyVO;

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


    PageResult<MoneyFlowPageVO> moneyFlowPageQuery(PageQuery pageQuery);

    SumIncomeMoneyVO sumIncomeMoneyQuery(SumIncomeMoneyQuery query);
}
