package com.lanf.order.mq.listener;

import com.lanf.aftersales.mq.message.CloseOrderMessage;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.CLOSE_ORDER_TOPIC,
        consumerGroup = OrderMqGroupName.CLOSE_ORDER_GROUP)

public class CloseOrderListener implements RocketMQListener<CloseOrderMessage> {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(CloseOrderMessage message) {

        log.info("订单完成,关闭订单:{}", message);
        orderService.closeOrderMessage(message);


    }


}