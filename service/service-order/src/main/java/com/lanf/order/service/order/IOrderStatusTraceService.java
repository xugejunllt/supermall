package com.lanf.order.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.order.model.entity.OrderStatusTraceDO;

/**
 * <p>
 * 订单状态溯源表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
public interface IOrderStatusTraceService extends IService<OrderStatusTraceDO> {

    @Deprecated
    void addOrderStatusTrace(Long orderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus);
    @Deprecated
    void addOrderStatusTrace(Long orderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus,String remark);
}
