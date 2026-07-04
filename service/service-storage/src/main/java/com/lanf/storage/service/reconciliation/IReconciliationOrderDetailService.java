package com.lanf.storage.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.storage.model.query.ReconciliationOrderDetailPageQuery;
import com.lanf.api.storage.model.vo.ReconciliationOrderDetailPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.model.bo.AddReconciliationOrderDetailBO;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;

/**
 * <p>
 * 库存对账订单详细 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
public interface IReconciliationOrderDetailService extends IService<ReconciliationOrderDetailDO> {

    /**
     * 分页查询库存对账订单详细
     */
    PageResult<ReconciliationOrderDetailPageVO> reconciliationOrderDetailPageQuery(ReconciliationOrderDetailPageQuery query);

    void addReconciliationOrderDetail(AddReconciliationOrderDetailBO bo);

}
