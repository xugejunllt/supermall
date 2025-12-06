package com.lanf.search.mq;

import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SyncGoodsInfoToEsMsg;
import com.lanf.search.service.IGoodsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
/**
 * 使用顺序消费
 */
@RocketMQMessageListener(topic = TopicName.SAVE_GOODS_ES_TOPIC,
        consumerGroup = TopicName.SAVE_GOODS_ES__GROUP,consumeMode = ConsumeMode.ORDERLY)
public class GoodsListener implements RocketMQListener<SyncGoodsInfoToEsMsg> {

    @Autowired
    private IGoodsInfoService goodsInfoService;

    @Override
    public void onMessage(SyncGoodsInfoToEsMsg message) {

        log.info("添加数据B");
        try {
            goodsInfoService.saveGoodsInfo(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}