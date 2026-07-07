package com.lanf.goods.mq.listener;

import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.PublishStockMessage;
import com.lanf.goods.mq.constant.GoodsMqGroupName;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.goods.service.stock.IUserStockPreorderPublishLogService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发布预售库存
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageClientTopicName.PUBLISH_STOCK_TOPIC,
        consumerGroup = GoodsMqGroupName.PUBLISH_STOCK_GROUP)
public class PublishStockListener implements RocketMQListener<PublishStockMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private IUserStockPreorderPublishLogService userStockPreorderPublishLogService;
    @Autowired
    private IGoodsSkuService goodsSkuService;

    /**
     * 路由到同个库 支持事务注解
     *
     */
    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(PublishStockMessage message) {

        log.info("收到预发售库存消息:{}",message);
        stockService.publishStock(message);

    }





}