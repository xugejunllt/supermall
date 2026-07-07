package com.lanf.goods.mq.listener;

import com.lanf.common.utils.JsonUtils;
import com.lanf.goods.mq.constant.GoodsMqGroupName;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.mq.constant.SecKillClientTopicName;
import com.lanf.seckill.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 秒杀成功 扣减库存
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
        consumerGroup = GoodsMqGroupName.DEDUCT_FROZEN_STOCK_GROUP)
public class SecKillPlaneOrderListener implements RocketMQListener<SecKillPlaneMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(SecKillPlaneMessage message) {

        log.info("秒杀成功,扣减库存开始:{}", JsonUtils.toJsonString(message));

        stockService.secKillPlane(message);
    }


}