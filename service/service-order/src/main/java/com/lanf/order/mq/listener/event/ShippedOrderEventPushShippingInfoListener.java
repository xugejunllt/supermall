package com.lanf.order.mq.listener.event;

import com.lanf.api.order.mq.message.OrderShippedMessage;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.order.mq.constant.OrderMqGroupName;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener( topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = OrderMqGroupName.SHIPPED_ORDER_EVENT_PUSH_SHIPPING_INFO,
        selectorExpression = OrderTopicWithTag.TAG_SHIPPED)
public class ShippedOrderEventPushShippingInfoListener implements RocketMQListener<OrderShippedMessage> {


    @Override
    public void onMessage(OrderShippedMessage orderShippedMessage) {

        log.info("监听到订单发货消息,推送物流信息到快递100:{}", orderShippedMessage);

    }
}
