package com.lanf.storage.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.api.storage.model.dto.PublishStockDTO;
import com.lanf.api.storage.model.query.StockPreorderPublishLogPageQuery;
import com.lanf.api.storage.model.vo.StockPreorderPublishLogPageVO;
import com.lanf.storage.model.entity.StockPreorderPublishLogDO;

/**
 * <p>
 * 库存预售发布记录 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-05
 */
public interface IStockPreorderPublishLogService extends IService<StockPreorderPublishLogDO> {
    /**
     * 发布预售库存
     */
    void publishStock(PublishStockDTO publishStock);

    /**
     * 分页查询库存预售发布日志
     */
    PageResult<StockPreorderPublishLogPageVO> stockPreorderPublishLogPageQuery(StockPreorderPublishLogPageQuery query);


}
