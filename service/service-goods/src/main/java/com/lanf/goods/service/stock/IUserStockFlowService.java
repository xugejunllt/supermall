package com.lanf.goods.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.api.goods.model.query.UserStockFlowPageQuery;
import com.lanf.api.goods.model.vo.ReconciliationStockFlowVO;
import com.lanf.api.goods.model.vo.UserStockFlowPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.entity.UserStockFlowDO;

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

    /**
     * 分页查询库存流水
     */
    PageResult<UserStockFlowPageVO> userStockFlowPageQuery(UserStockFlowPageQuery query);
}
