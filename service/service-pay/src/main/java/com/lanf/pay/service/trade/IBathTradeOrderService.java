package com.lanf.pay.service.trade;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.pay.model.entity.BathTradeOrderDO;

/**
 * <p>
 * 批量交易订单 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
public interface IBathTradeOrderService extends IService<BathTradeOrderDO> {
    /**
     * 购物车提交订单时 创建批量交易订单
     *
     *
     */
    void createMergeTradeOrder(CreateMergeTradeOrderDTO dto);




}
