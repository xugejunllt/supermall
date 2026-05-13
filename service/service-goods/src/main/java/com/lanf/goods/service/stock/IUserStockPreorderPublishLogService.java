package com.lanf.goods.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.dto.RecycleStockDTO;
import com.lanf.goods.model.entity.UserStockPreorderPublishLogDO;

/**
 * <p>
 * 库存预售发布记录 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-05
 */
public interface IUserStockPreorderPublishLogService extends IService<UserStockPreorderPublishLogDO> {


    /**
     * 回收库存
     *
     *
     */
    void recycleStock(RecycleStockDTO recycleStockDTO);
}
