package com.lanf.order.mq.listener;

import com.lanf.aftersales.mq.message.CloseOrderMessage;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


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


        //进行结算

    }


}