package com.lanf.order.mq.listener;

import com.lanf.order.model.dto.CancelOrderDTO;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.mq.constant.OrderMqTopicName;
import com.lanf.order.mq.message.SecKillOrderCancelMessage;
import com.lanf.order.mq.message.SecKillOrderCreatedMessage;
import com.lanf.order.service.OrderManagerService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 取消超时未确认的订单
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = OrderMqTopicName.SEC_KILL_ORDER_CANCEL_TOPIC,
        consumerGroup = OrderMqGroupName.SEC_KILL_ORDER_TIMEOUT_CANCEL_TOPIC)
public class SecKillOrderTimeoutCancelListener implements RocketMQListener<SecKillOrderCancelMessage> {


    @Autowired
    private OrderManagerService orderManagerService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(SecKillOrderCancelMessage message) {


        /**
         * 创建取消后回调函数
         */
        Runnable runnable = () -> {
            SecKillOrderCreatedMessage message1 = new SecKillOrderCreatedMessage();
            message1.setOrderNumber(message.getOrderNumber());
            message1.setResult( false);
            rocketMqClient.sendMessage(OrderClientTopicName.SEC_KILL_ORDER_CREATED_TOPIC, message);
        };
        CancelOrderDTO dto = new CancelOrderDTO();
        dto.setOrderId(message.getOrderId());
        dto.setRemark("秒杀订单超时未确认，进行自动取消");
        dto.setRunnable(runnable);

        orderManagerService.cancelOrder(dto);



    }
}
