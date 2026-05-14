package com.lanf.storage.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.api.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.rocketmq.model.enums.DelayLevelEnum;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.api.storage.mq.message.AddReconciliationOrderDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听订单创建成功事件添加订单对账单  用于库存对账
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = StorageMqGroupName.ORDER_CREATE_SUCCESS_EVENT_ADD_RECONCILIATION_ORDER_GROUP,
        topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumeMode = ConsumeMode.ORDERLY,
        selectorExpression = OrderTopicWithTag.TAG_WAIT_PAY
)
public class OrderCreateSuccessEventListener implements RocketMQListener<OrderCreateSuccessMessage> {

     @Autowired
     private RocketMqClient rocketMqClient;


     @Override
     public void onMessage(OrderCreateSuccessMessage message) {

          AddReconciliationOrderDetail message2 = new AddReconciliationOrderDetail();
          message2.setOrderId(message.getOrderId());
          message2.setToOrderStatus(OrderStatusEnum.WAIT_PAY);
          message2.setUserStockFlowEventType(UserStockFlowEventTypeEnum.ORDER_OUTBOUND);
          /**
           * 发送延迟消息 10 分钟 下游数据可能还没有插入成功
           */
          rocketMqClient.sendDelayMessage(StorageClientTopicName.ADD_RECONCILIATION_ORDER_TOPIC,
                  JsonUtils.toJsonString(message2), DelayLevelEnum.LEVEL_14);



     }




}