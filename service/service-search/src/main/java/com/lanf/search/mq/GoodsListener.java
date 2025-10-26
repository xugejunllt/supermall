package com.lanf.search.mq;

import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.GoodsAddMsg;
import com.lanf.search.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.SAVE_GOODS_ES_TOPIC, consumerGroup = TopicName.SAVE_GOODS_ES__GROUP)
public class GoodsListener implements RocketMQListener<GoodsAddMsg> {

    @Autowired
    private GoodsService goodsService;

    @ConsumeMessage
    @Override
    public void onMessage(GoodsAddMsg message) {

        log.info("保存商品到ES:{}", message);
        goodsService.addGoods(message);



    }
}