package com.lanf.order.mq.listener;

import com.lanf.aftersales.mq.AftersalesClientTopicName;
import com.lanf.aftersales.mq.message.CloseOrderMessage;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = AftersalesClientTopicName.AFTER_SALES_CLOSE_ORDER_TOPIC, consumerGroup = OrderMqGroupName.AFTER_SALES_CLOSE_ORDER_GROUP)
public class CloseOrderListener implements RocketMQListener<CloseOrderMessage> {

    @Autowired
    private IOrderService orderService;

    @Override
    public void onMessage(CloseOrderMessage message) {

        log.info("售后完成,关闭订单:{}", message);

        Long orderId = message.getOrderId();
        OrderDO orderDO = orderService.getById(orderId);
        if (orderDO == null){
            log.error("订单不存在:{}", orderId);
            return;
        }
        Integer status = orderDO.getStatus();
        if ( !(OrderStatusEnum.WAIT_COMMENT.getCode().equals(status)
           || OrderStatusEnum.COMPLETED.getCode().equals(status))){

            log.error("订单状态错误:{}", status);
            return;
        }
        boolean update = orderService.lambdaUpdate()
                .eq(OrderDO::getId, orderId)
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.CLOSED.getCode())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.error("订单更新失败:{}", orderId);

        }

    }

















}