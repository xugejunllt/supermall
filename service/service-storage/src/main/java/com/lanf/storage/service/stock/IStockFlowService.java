package com.lanf.storage.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.api.storage.model.query.StockFlowPageQuery;
import com.lanf.api.storage.model.vo.StockFlowPageVO;

/**
 * <p>
 * 库存流水 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
public interface IStockFlowService extends IService<StockFlowDO> {

    PageResult<StockFlowPageVO> stockFlowPageQuery(StockFlowPageQuery query);


}
