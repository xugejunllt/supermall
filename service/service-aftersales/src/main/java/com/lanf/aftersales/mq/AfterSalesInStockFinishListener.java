package com.lanf.aftersales.mq;

import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.storage.mq.StorageClientTopicName;
import com.lanf.storage.mq.message.AfterSalesInStockFinishMessage;
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
@RocketMQMessageListener(topic =  StorageClientTopicName.AFTER_SALES_IN_STOCK_FINISH_TOPIC, consumerGroup = StorageClientTopicName.AFTER_SALES_IN_STOCK_FINISH_GROUP)
public class AfterSalesInStockFinishListener implements RocketMQListener<AfterSalesInStockFinishMessage> {

    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;

    @Override
    public void onMessage(AfterSalesInStockFinishMessage message) {

        log.info("售后入库单已完成入库：{}", JsonUtils.toJsonString( message));
        afterSalesOrderService.afterSalesInStockFinish(message.getAfterSalesOrderId());

    }
}