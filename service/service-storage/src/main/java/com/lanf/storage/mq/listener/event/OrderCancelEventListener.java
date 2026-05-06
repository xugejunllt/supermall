package com.lanf.storage.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.goods.model.enums.UserStockFlowEventTypeEnum;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.rocketmq.model.enums.DelayLevelEnum;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.message.AddReconciliationOrderDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听订单创建成功事件添加订单对账单
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.ORDER_CANCEL_EVENT_TOPIC,
        consumerGroup = StorageMqGroupName.ORDER_CANCEL_EVENT_ADD_RECONCILIATION_ORDER_GROUP)
public class OrderCancelEventListener implements RocketMQListener<OrderCreateSuccessMessage> {

     @Autowired
     private RocketMqClient rocketMqClient;


     @Override
     public void onMessage(OrderCreateSuccessMessage message) {

          AddReconciliationOrderDetail message2 = new AddReconciliationOrderDetail();
          message2.setOrderId(message.getOrderId());
          message2.setToOrderStatus(OrderStatusEnum.CANCELLED);
          message2.setUserStockFlowEventType(UserStockFlowEventTypeEnum.CANCEL_ORDER_INBOUND);
          /**
           * 发送延迟消息 10 分钟 下游数据可能还没有插入成功
           */
          rocketMqClient.sendDelayMessage(StorageClientTopicName.ADD_RECONCILIATION_ORDER_TOPIC,
                  JsonUtils.toJsonString(message2), DelayLevelEnum.LEVEL_14);



     }




}