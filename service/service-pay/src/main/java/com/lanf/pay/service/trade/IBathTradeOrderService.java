package com.lanf.pay.service.trade;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.dto.BathCreatePrepayOrderDTO;
import com.lanf.client.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.pay.model.entity.BathTradeOrderDO;
import com.lanf.pay.model.vo.CreatePrepayOrderVO;

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

    /**
     * 批量创建预支付信息
     *
     */
    CreatePrepayOrderVO bathCreatePrepayOrder(BathCreatePrepayOrderDTO dto);


}
