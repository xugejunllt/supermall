package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.order.mapper.OrderStatusTraceMapper;
import com.lanf.order.model.bo.AddOrderStatusTrace;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.service.IOrderStatusTraceService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单状态溯源表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
@Service
public class OrderStatusTraceServiceImpl extends ServiceImpl<OrderStatusTraceMapper, OrderStatusTraceDO> implements IOrderStatusTraceService {

    @Override
    public void addOrderStatusTrace(AddOrderStatusTrace addOrderStatusTrace) {


        OrderStatusEnum fromStatus = addOrderStatusTrace.getFromStatus();
        OrderStatusEnum toStatus = addOrderStatusTrace.getToStatus();





    }
}
