package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.DateUtils;
import com.lanf.order.mapper.OrderStatusTraceMapper;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.service.IOrderStatusTraceService;
import org.springframework.stereotype.Service;

import java.util.Date;

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
    public void addOrderStatusTrace(Long orderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus) {

        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(fromStatus);
        orderStatusTraceDO.setToStatus(toStatus);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        this.save(orderStatusTraceDO);





    }

    @Override
    public void addOrderStatusTrace(Long orderId, OrderStatusEnum fromStatus, OrderStatusEnum toStatus, String remark) {
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(fromStatus);
        orderStatusTraceDO.setToStatus(toStatus);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setRemark(remark);
        this.save(orderStatusTraceDO);
    }
}
