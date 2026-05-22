package com.lanf.storage.mq.listener;

/**
 * 售后退货创建商品入库单
 */

import com.lanf.common.utils.JsonUtils;
import com.lanf.aftersales.mq.AftersalesClientTopicName;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.storage.service.storage.IAfterSalesIntStockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = AftersalesClientTopicName.AFTER_SALES_CREATE_IN_ORDER_TOPIC, consumerGroup =
        AftersalesClientTopicName.AFTER_SALES_CREATE_IN_ORDER_GROUP)
public class AfterSalesCreateInOrderListener implements RocketMQListener<SalesInStockOrderAddMessage> {

    @Autowired
    private IAfterSalesIntStockOrderService afterSalesIntStockOrderService;



    @Override
    public void onMessage(SalesInStockOrderAddMessage message) {

        log.info("监听到售后单商家签收商品消息:{}", JsonUtils.toJsonString(message));

        afterSalesIntStockOrderService.addAfterSalesIntStockOrder( message);
    }




}
