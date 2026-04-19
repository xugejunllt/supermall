package com.lanf.goods.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.goods.service.goods.IStockService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.CANCEL_ORDER_EVENT_TOPIC, consumerGroup = TopicName.CANCEL_ORDER_EVENT_GOODS_GROUP)
public class CancelOrderEventRollbackStockListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private IStockService stockService;


    @Override
    public void onMessage(CancelOrderEventMessage message) {
        log.info("取消订单事件回滚库存开始:[{{}}]", JsonUtils.toJsonString(message));





















    }

}