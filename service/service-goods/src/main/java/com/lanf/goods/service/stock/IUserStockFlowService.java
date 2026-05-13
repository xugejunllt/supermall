package com.lanf.goods.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.api.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.api.goods.model.vo.ReconciliationStockFlowVO;

/**
 * <p>
 * 库存流水 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
public interface IUserStockFlowService extends IService<UserStockFlowDO> {

    ReconciliationStockFlowVO reconciliationStockFlowQuery(ReconciliationStockFlowQuery query);
}
