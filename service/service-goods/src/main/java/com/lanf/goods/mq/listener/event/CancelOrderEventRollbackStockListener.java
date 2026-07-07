package com.lanf.goods.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.goods.model.bo.RollbackStockBO;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.model.message.OrderGoodsInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 回滚商品库存
 */

@Slf4j
@Component
@RocketMQMessageListener( topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = TopicName.CANCEL_ORDER_EVENT_GOODS_GROUP,
          selectorExpression = OrderTopicWithTag.TAG_CANCELLED)
public class CancelOrderEventRollbackStockListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private IStockService stockService;

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(CancelOrderEventMessage message) {

        log.info("取消订单消息,回滚库存开始:{}", JsonUtils.toJsonString(message));
        List<OrderGoodsInfo> orderGoodsInfoList = message.getOrderGoodsInfoList();
        for (OrderGoodsInfo orderGoodsInfo : orderGoodsInfoList) {

            RollbackStockBO rollbackStockBO = new RollbackStockBO();
            rollbackStockBO.setOrderId(message.getOrderId());
            rollbackStockBO.setOrderNumber(message.getOrderNumber());
            rollbackStockBO.setOrderGoodsInfo(orderGoodsInfo);
            stockService.rollbackStock(rollbackStockBO);
        }

        log.info("取消订单消息,回滚库存成功");
    }





}