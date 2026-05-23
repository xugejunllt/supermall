package com.lanf.order.mq.listener;

import com.lanf.api.order.model.enums.OrderProcessStepEnum;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.SecKillPlaneCreateOrderSuccessMessage;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.utils.IdUtils;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.enums.OrderTypeEnum;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.mq.constant.OrderMqTopicName;
import com.lanf.order.mq.message.SecKillOrderCancelMessage;
import com.lanf.order.service.order.IOrderItemService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.welfare.mq.constant.SecKillClientTopicName;
import com.lanf.welfare.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RocketMQMessageListener(topic = SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
        consumerGroup = OrderMqGroupName.SEC_KILL_PLANE_GROUP)
public class SecKillPlaneOrderListener implements RocketMQListener<SecKillPlaneMessage> {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderItemService orderItemService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;
    @Autowired
    private RocketMqClient rocketMqClient ;

    @Transactional
    @Override
    public void onMessage(SecKillPlaneMessage message) {

        log.info("接收到秒杀单消息：{}", message);
        BigDecimal totalMoney = BigDecimalUtils.multiply(message.getUnitPrice(), BigDecimal.valueOf(message.getQuantity()));
        Long orderId = IdUtils.generateId();
        OrderDO orderDO = getOrderDO(message, orderId, totalMoney);
        //
        OrderItemDO orderItemDO = new OrderItemDO();
        orderItemDO.setOrderId(orderId);
        orderItemDO.setGoodsId(message.getGoodsId());
        orderItemDO.setGoodsName(message.getGoodsName());
        orderItemDO.setGoodsTitle(message.getGoodsTitle());
        orderItemDO.setSkuId(message.getSkuId());
        orderItemDO.setSkuCode(message.getSkuCode());
        orderItemDO.setSkuName(message.getSkuName());
        orderItemDO.setSkuPictureAddress(message.getSkuPictureAddress());
        orderItemDO.setQuantity(message.getQuantity());
        orderItemDO.setUnitPrice(message.getUnitPrice());
        orderItemDO.setGoodsVersion(message.getGoodsVersion());
        orderItemDO.setSkuVersion(message.getSkuVersion());
        orderItemDO.setWarehouseId(message.getWarehouseId());
        orderItemDO.setUserId(message.getUserId());
        orderItemDO.setTenantId(message.getTenantId());
        //

        SecKillPlaneCreateOrderSuccessMessage successMessage = new SecKillPlaneCreateOrderSuccessMessage();
        successMessage.setOrderId(orderId);
        successMessage.setOrderNumber(message.getOrderNumber());
        successMessage.setUserId(message.getUserId());
        successMessage.setTradeMoney(totalMoney);
        successMessage.setStockFlowNo(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.STOCK_FLOW,
                message.getOrderNumber()));
        successMessage.setSkuCode(message.getSkuCode());
        successMessage.setWarehouseId(message.getWarehouseId());
        successMessage.setQuantity(message.getQuantity());

        //
        SecKillOrderCancelMessage message1 = new SecKillOrderCancelMessage();
        message1.setOrderId(orderId);
        message1.setOrderNumber(message.getOrderNumber());
        //
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(null);
        orderStatusTraceDO.setToStatus(OrderStatusEnum.WAIT_CONFIRM);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setTenantId(message.getTenantId());
        orderStatusTraceDO.setUserId(message.getUserId());
        try {
            orderService.save(orderDO);
        } catch (DuplicateKeyException e) {
             log.warn("订单已创建");
             return;
        }
        orderItemService.save(orderItemDO);
        orderStatusTraceService.save(orderStatusTraceDO);
        rocketMqClient.sendMessage(OrderClientTopicName.SEC_KILL_PLANE_CREATE_ORDER_SUCCESS_EVENT_TOPIC,
                JsonUtils.toJsonString(successMessage));
        /**
         * 发送延迟消息 10分钟如果订单没有确认完成 那么进行取消
         */
        rocketMqClient.sendDelayMessage(OrderMqTopicName.SEC_KILL_ORDER_CANCEL_TOPIC,
                JsonUtils.toJsonString(message1), TimeUnit.MINUTES, 10);
    }

    private static OrderDO getOrderDO(SecKillPlaneMessage message, Long orderId, BigDecimal totalMoney) {
        OrderDO orderDO = new OrderDO();
        orderDO.setId(orderId);
        orderDO.setShopId(message.getShopId());
        orderDO.setShopName(message.getShopName());
        orderDO.setTenantId(message.getTenantId());
        orderDO.setUserId(message.getUserId());
        orderDO.setOrderNumber(message.getOrderNumber());
        orderDO.setTotalMoney(totalMoney);
        orderDO.setActualPayMoney(totalMoney);
        orderDO.setTakeAddress(message.getTakeAddress());
        orderDO.setStatus(OrderStatusEnum.WAIT_CONFIRM);
        orderDO.setOrderType(OrderTypeEnum.SEC_KILL);
        orderDO.setOrderProcessSteps(OrderProcessStepEnum.ORDER_CREATED.getCode().toString());
        orderDO.setAfterSaleDays(message.getAfterSaleDays());
        orderDO.setDiscountAmount(new BigDecimal(0));
        return orderDO;
    }

}