package com.lanf.order.service.aftersales.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.AfterSalesOrderMapper;
import com.lanf.order.model.dto.AddAfterSalesOrderDTO;
import com.lanf.order.model.entity.AfterSalesOrderDO;
import com.lanf.order.model.entity.AfterSalesOrderItemDO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.enums.MainStatusEnum;
import com.lanf.order.model.enums.SubStatus;
import com.lanf.order.service.aftersales.IAfterSalesOrderItemService;
import com.lanf.order.service.aftersales.IAfterSalesOrderService;
import com.lanf.order.service.order.IOrderItemService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 售后单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-19
 */
@Slf4j
@Service
public class AfterSalesOrderServiceImpl extends ServiceImpl<AfterSalesOrderMapper, AfterSalesOrderDO> implements IAfterSalesOrderService {

    @Autowired
    private RocketMqClient rocketMqClient;


    @Autowired
    private IAfterSalesOrderItemService afterSalesOrderItemService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderItemService orderItemService;

    @Override
    public void addAfterSalesOrder(AddAfterSalesOrderDTO dto) {


        Long orderId = dto.getOrderId();
        Long userId = UserContext.getUserId();
        /**
         * 校验
         */
        AfterSalesOrderDO salesOrderDO = this.lambdaQuery()
                .eq(AfterSalesOrderDO::getOrderId, orderId).one();
        if (salesOrderDO != null){
            if (! (MainStatusEnum.CLOSED
                    .equals(salesOrderDO.getMainStatus())
                    || MainStatusEnum.SUCCESS
                    .equals(salesOrderDO.getMainStatus()))) {
                throw new BizException("已存在处理中的售后单");
            }
        }

        OrderDO one = orderService.lambdaQuery().eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, userId)
                .eq(BaseEntity::getId, orderId).one();
        if (one == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
//        if (!(OrderStatusEnum.WAIT_COMMENT.equals(one.getStatus())
//                || OrderStatusEnum.COMPLETED.equals(one.getStatus()))) {
//            log.warn("当前订单状态不支持售后");
//            throw new BizException("当前订单状态不支持售后");
//        }

        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, orderId)
                .eq(OrderItemDO::getUserId, userId)
                .list();

        AfterSalesOrderDO afterSalesOrder = new AfterSalesOrderDO();
        Long id = IdUtils.generateId();
        afterSalesOrder.setUserId(UserContext.getUserId());
        afterSalesOrder.setId(id);
        afterSalesOrder.setOrderId(orderId);
        afterSalesOrder.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
        afterSalesOrder.setAfterSalesType(dto.getAfterSalesType());
        /**
         * 1天自动审核通过
         */
        afterSalesOrder.setBusinessAutoAgreeTime(new Date(DateUtils.getExpireTimestampFromDays(1)));
        afterSalesOrder.setReturnReason(dto.getReturnReason());
        afterSalesOrder.setMainStatus(MainStatusEnum.WAIT_SELLER_AGREE);
        afterSalesOrder.setSubStatus(SubStatus.WAIT_MANUAL);
        afterSalesOrder.setReturnMoney(one.getActualPayMoney());
        afterSalesOrder.setTenantId(one.getTenantId());
        //
        List<AfterSalesOrderItemDO> saveAfterSalesOrderItem = new ArrayList<>(orderItemDOList.size());
        for (OrderItemDO a : orderItemDOList) {
            AfterSalesOrderItemDO afterSalesOrderItem = new AfterSalesOrderItemDO();
            afterSalesOrderItem.setAfterSalesOrderId(id);
            afterSalesOrderItem.setGoodsId(a.getGoodsId());
            afterSalesOrderItem.setGoodsName(a.getGoodsName());
            afterSalesOrderItem.setSkuName(a.getSkuName());
            afterSalesOrderItem.setSkuCode(a.getSkuCode());
            afterSalesOrderItem.setSkuPictureAddress(a.getSkuPictureAddress());
            afterSalesOrderItem.setQuantity(a.getQuantity());
            afterSalesOrderItem.setUnitPrice(a.getUnitPrice());
            afterSalesOrderItem.setTotalMoney(BigDecimalUtil.multiply(a.getUnitPrice(), new
                    BigDecimal(a.getQuantity())));
            afterSalesOrderItem.setTenantId(one.getTenantId());
            saveAfterSalesOrderItem.add(afterSalesOrderItem);
        }
        /**
         * 保存
         */
        this.save(afterSalesOrder);
        afterSalesOrderItemService.saveBatch(saveAfterSalesOrderItem);
        /**
         * 发送延迟消息 商家自动同意
         */
    }


}
