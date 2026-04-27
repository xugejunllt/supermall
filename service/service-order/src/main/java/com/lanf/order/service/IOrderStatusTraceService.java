package com.lanf.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.order.model.bo.AddOrderStatusTrace;
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


    void addOrderStatusTrace(AddOrderStatusTrace addOrderStatusTrace);
}
