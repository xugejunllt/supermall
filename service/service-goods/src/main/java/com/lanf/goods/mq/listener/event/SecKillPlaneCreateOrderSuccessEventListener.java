package com.lanf.goods.mq.listener.event;

import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.SecKillPlaneCreateOrderSuccessMessage;
import com.lanf.goods.mq.constant.GoodsMqGroupName;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀 订单创建成功之后 ，创建交易单
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderClientTopicName.SEC_KILL_PLANE_CREATE_ORDER_SUCCESS_EVENT_TOPIC,
        consumerGroup = GoodsMqGroupName.DEDUCT_FROZEN_STOCK_GROUP
)
public class SecKillPlaneCreateOrderSuccessEventListener implements RocketMQListener<SecKillPlaneCreateOrderSuccessMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(SecKillPlaneCreateOrderSuccessMessage message) {



    }
}
