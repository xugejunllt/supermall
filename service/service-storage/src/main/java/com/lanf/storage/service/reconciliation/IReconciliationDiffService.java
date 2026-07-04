package com.lanf.storage.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.storage.model.query.ReconciliationDiffPageQuery;
import com.lanf.api.storage.model.vo.ReconciliationDiffPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.model.entity.ReconciliationDiffDO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
public interface IReconciliationDiffService extends IService<ReconciliationDiffDO> {

    /**
     * 分页查询对账差异
     */
    PageResult<ReconciliationDiffPageVO> reconciliationDiffPageQuery(ReconciliationDiffPageQuery query);

}
