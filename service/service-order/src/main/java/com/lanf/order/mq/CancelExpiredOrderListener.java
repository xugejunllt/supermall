package com.lanf.order.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.order.model.dto.CancelOrderDTO;
import com.lanf.order.service.OrderManagerService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelExpiredOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 交易成功事件 - 支付服务消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = TopicName.CANCEL_EXPIRED_ORDER_TOPIC,
    consumerGroup = TopicName.CANCEL_EXPIRED_ORDER_GROUP
)
public class CancelExpiredOrderListener implements RocketMQListener<CancelExpiredOrderMessage> {

    @Autowired
    private OrderManagerService orderManagerService;

    @Override
    public void onMessage(CancelExpiredOrderMessage message) {

        log.info("取消超时未支付的订单{}", JsonUtils.toJsonString(message));
        CancelOrderDTO dto = new CancelOrderDTO();
        dto.setOrderId(message.getOrderId());
        dto.setCancelSource(message.getCancelSource());
        orderManagerService.cancelOrder( dto);



    }
}
