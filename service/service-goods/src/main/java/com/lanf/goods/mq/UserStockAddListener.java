package com.lanf.goods.mq;

import com.lanf.goods.service.goods.IStockService;
import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.UserStockAddMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.USER_STOCK_ADD_TOPIC, consumerGroup = TopicName.USER_STOCK_ADD_GROUP)
public class UserStockAddListener implements RocketMQListener<UserStockAddMsg> {

    @Autowired
    private IStockService stockService;

    @ConsumeMessage
    @Override
    public void onMessage(UserStockAddMsg message) {

        stockService.addUserStock(message);
    }

}