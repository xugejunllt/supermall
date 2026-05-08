package com.lanf.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.enums.OrderStatusEnum;

/**
 * <p>
 * 订单状态溯源表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
public interface IOrderStatusTraceService extends IService<OrderStatusTraceDO> {


    void addOrderStatusTrace(Long orderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus);

    void addOrderStatusTrace(Long orderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus,String remark);
}
