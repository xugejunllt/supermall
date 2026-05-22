package com.lanf.order.mq.listener;

import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.SalesOutStockOrderFinishMessage;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.order.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 销售出库单出库完成监听
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = StorageClientTopicName.SALES_OUT_STOCK_ORDER_FINISH_TOPIC,
    consumerGroup = OrderMqGroupName.SALES_OUT_STOCK_ORDER_FINISH_GROUP
)
public class SalesOutStockOrderFinishListener implements RocketMQListener<SalesOutStockOrderFinishMessage> {

    @Autowired
    private IOrderService orderService;

    @Override
    public void onMessage(SalesOutStockOrderFinishMessage message) {

        log.info("出库完成，修改订单状态");
        orderService.outStockFinish(message);



    }
}
