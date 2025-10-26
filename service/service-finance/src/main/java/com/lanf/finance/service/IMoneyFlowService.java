package com.lanf.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.finance.model.entity.MoneyFlowDO;
import com.lanf.finance.model.query.AccountMoneySumQuery;
import com.lanf.finance.model.query.MoneyFlowPageQuery;
import com.lanf.finance.model.vo.AccountMoneySumVO;
import com.lanf.mybatis.base.PageResult;
import com.lanf.rocketmq.model.message.MoneyFlowDTO;

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
     * 批量添加资金流水
     *
     */
    void moneyFlowAdd( MoneyFlowDTO dto);

    PageResult<MoneyFlowDO> moneyFlowPage(MoneyFlowPageQuery query );

    /**
     * 账户-科目金额汇总
     *
     *
     */
    AccountMoneySumVO accountMoneySumQuery(AccountMoneySumQuery query);



}
