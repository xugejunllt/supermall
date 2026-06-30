package com.lanf.pay.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import com.lanf.api.pay.model.query.ReconciliationDiffPageQuery;
import com.lanf.api.pay.model.vo.ReconciliationDiffPageVO;

/**
 * <p>
 * 对账差异明细表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface IReconciliationDiffService extends IService<ReconciliationDiffDO> {


    PageResult<ReconciliationDiffPageVO> reconciliationDiffPageQuery(ReconciliationDiffPageQuery query);
}
