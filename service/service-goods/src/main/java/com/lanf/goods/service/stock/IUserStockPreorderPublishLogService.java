package com.lanf.goods.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.dto.RecycleStockDTO;
import com.lanf.api.goods.model.query.UserStockPreorderPublishLogPageQuery;
import com.lanf.api.goods.model.vo.UserStockPreorderPublishLogPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.entity.UserStockPreorderPublishLogDO;

/**
 * <p>
 * 库存预售发布日志 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
public interface IUserStockPreorderPublishLogService extends IService<UserStockPreorderPublishLogDO> {


    /**
     * 回收库存
     *
     *
     */
    void recycleStock(RecycleStockDTO recycleStockDTO);

    /**
     * 分页查询库存预售发布日志
     */
    PageResult<UserStockPreorderPublishLogPageVO> userStockPreorderPublishLogPageQuery(UserStockPreorderPublishLogPageQuery query);
}
