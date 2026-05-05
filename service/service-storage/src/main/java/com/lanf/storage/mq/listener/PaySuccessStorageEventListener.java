package com.lanf.storage.mq.listener;


import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.PAY_SUCCESS_EVENT_TOPIC, consumerGroup = TopicName.PAY_SUCCESS_STORAGE_EVENT_GROUP)
public class PaySuccessStorageEventListener implements RocketMQListener<PaySuccessEventMessage> {

    @Autowired
    private ISalesOutStockOrderService salesOutStockOrderService;

    @Override
    public void onMessage(PaySuccessEventMessage paySuccessEventMessage) {

        log.info("支付成功，添加销售出库单");
        //订单支付成功
        salesOutStockOrderService.salesOutStockOrderAdd(paySuccessEventMessage.getOrderId());

    }

}
