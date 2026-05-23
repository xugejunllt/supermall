package com.lanf.order.mq.listener;

import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.AfterSalesInStockFinishMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.aftersales.IAfterSalesOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 售后出库单出库完成
 */

@Slf4j
@Component
@RocketMQMessageListener(topic =  StorageClientTopicName.AFTER_SALES_IN_STOCK_FINISH_TOPIC,
        consumerGroup = OrderMqGroupName.AFTER_SALES_IN_STOCK_FINISH_GROUP
)
public class AfterSalesInStockFinishListener implements RocketMQListener<AfterSalesInStockFinishMessage> {

    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;

    @Override
    public void onMessage(AfterSalesInStockFinishMessage message) {

        log.info("监听到售后入库单已完成入库消息：{}", JsonUtils.toJsonString( message));
        afterSalesOrderService.afterSalesInStockFinish(message.getAfterSalesOrderId());

    }
}