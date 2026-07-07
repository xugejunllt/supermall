package com.lanf.storage.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.storage.model.bo.AddReconciliationOrderDetailBO;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = StorageMqGroupName.ORDER_CANCEL_EVENT_ADD_RECONCILIATION_ORDER_GROUP,
        topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        selectorExpression = OrderTopicWithTag.TAG_CANCELLED)
public class OrderCancelEventListener implements RocketMQListener<CancelOrderEventMessage> {


     @Autowired
     private IReconciliationOrderDetailService reconciliationOrderDetailService;

     @MqRetryConsume(messageId = "#message.messageId")
     @Override
     public void onMessage(CancelOrderEventMessage message) {

          log.info("取消订单消息,添加对账单开始:{}", JsonUtils.toJsonString(message));
          AddReconciliationOrderDetailBO bo = new AddReconciliationOrderDetailBO();
          bo.setOrderId(message.getOrderId());
          bo.setToOrderStatus(OrderStatusEnum.CANCELLED);
          bo.setUserStockFlowEventType(UserStockFlowEventTypeEnum.CANCEL_ORDER_INBOUND);
          reconciliationOrderDetailService.addReconciliationOrderDetail(bo);

     }




}