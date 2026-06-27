package com.lanf.order.mq.listener;

import com.lanf.aftersales.mq.message.CloseOrderMessage;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.common.utils.DateUtils;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.CLOSE_ORDER_TOPIC,
        consumerGroup = OrderMqGroupName.CLOSE_ORDER_GROUP)

public class CloseOrderListener implements RocketMQListener<CloseOrderMessage> {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;

    @Transactional
    @Override
    public void onMessage(CloseOrderMessage message) {

        log.info("订单完成,关闭订单:{}", message);

        Long orderId = message.getOrderId();
        OrderDO orderDO = orderService.lambdaQuery()
                .eq(BaseEntity::getId, orderId)
                .eq(OrderDO::getUserId, message.getUserId())
                .one();

        if (orderDO == null) {
            log.error("订单不存在:{}", orderId);
            return;
        }
        OrderStatusEnum status = orderDO.getStatus();

        if (OrderStatusEnum.CLOSED.equals(status)){
            log.warn("订单已关闭");
            return;
        }

        if (!(OrderStatusEnum.RECEIVED.equals(status)

                || OrderStatusEnum.CANCELLED.equals(status))) {
            log.error("订单状态错误:{}", status);
            return;
        }

        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(orderDO.getStatus());
        orderStatusTraceDO.setToStatus(OrderStatusEnum.CLOSED);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setUserId(orderDO.getUserId());
        orderStatusTraceDO.setTenantId(orderDO.getTenantId());

        boolean update = orderService.lambdaUpdate()
                .eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, message.getUserId())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.CLOSED)
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("订单更新失败:{}", orderId);
            throw new MessageRetryConsumeException("订单更新失败");
        }
        orderStatusTraceService.save(orderStatusTraceDO);

        //进行结算

    }


}