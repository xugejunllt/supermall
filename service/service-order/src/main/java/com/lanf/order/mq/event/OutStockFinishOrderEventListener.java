package com.lanf.order.mq.event;


import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.order.service.IOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.OUT_STOCK_FINISH_EVENT_TOPIC, consumerGroup = TopicName.OUT_STOCK_FINISH_ORDER_EVENT_GROUP)
public class OutStockFinishOrderEventListener implements RocketMQListener<PaySuccessEventMessage> {

    @Autowired
    private IOrderService orderService;
    @ConsumeMessage
    @Override
    public void onMessage(PaySuccessEventMessage paySuccessEventMessage) {

        log.info("出库完成，修改订单状态");
        orderService.outStockFinish(paySuccessEventMessage.getOrderId());
    }

}
