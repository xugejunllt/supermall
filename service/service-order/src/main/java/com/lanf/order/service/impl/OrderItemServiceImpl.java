package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.order.mapper.OrderItemMapper;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.order.model.vo.ReconciliationOrderItem;
import com.lanf.order.model.vo.ReconciliationOrderItemVO;
import com.lanf.order.service.IOrderItemService;
import com.lanf.order.service.IOrderStatusTraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单商品项目 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemDO> implements IOrderItemService {

    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;

    @Override
    public ReconciliationOrderItemVO reconciliationOrderItemQuery(ReconciliationOrderItemQuery query) {


        OrderStatusTraceDO one = orderStatusTraceService.lambdaQuery()
                .eq(OrderStatusTraceDO::getOrderId, query.getOrderId())
                .eq(OrderStatusTraceDO::getToStatus, query.getOrderStatus())
                .one();
        if (one == null) {
            log.error("订单轨迹不存在");
            throw new BizException("订单轨迹不存在");
        }
        List<OrderItemDO> orderItemList = this.lambdaQuery()
                .eq(OrderItemDO::getOrderId, query.getOrderId())
                .list();

        if (IStringUtils.isEmpty(orderItemList)) {
            log.error("订单项目不存在");
            throw new BizException("订单项目不存在");
        }

        List<ReconciliationOrderItem> itemVOList = orderItemList.stream().map(orderItem -> {
            ReconciliationOrderItem itemVO = new ReconciliationOrderItem();
            itemVO.setQuantity(orderItem.getQuantity());
            itemVO.setSkuCode(orderItem.getSkuCode());
            itemVO.setWarehouseId(orderItem.getWarehouseId());
            return itemVO;
        }).collect(Collectors.toList());

        ReconciliationOrderItemVO vo = new ReconciliationOrderItemVO();
        vo.setCreateDate(one.getCreateDate());
        vo.setOrderItemVOS(itemVOList);
        return vo;
    }


}
