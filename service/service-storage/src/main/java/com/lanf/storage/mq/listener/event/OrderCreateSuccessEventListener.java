package com.lanf.storage.mq.listener.event;

import com.lanf.api.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.storage.model.bo.AddReconciliationOrderDetailBO;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import lombok.extern.slf4j.Slf4j;
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
        selectorExpression = OrderTopicWithTag.TAG_WAIT_PAY
)
public class OrderCreateSuccessEventListener implements RocketMQListener<OrderCreateSuccessMessage> {

     @Autowired
     private IReconciliationOrderDetailService reconciliationOrderDetailService;

     @MqRetryConsume(messageId = "#message.messageId")
     @Override
     public void onMessage(OrderCreateSuccessMessage message) {

          log.info("订单创建成功消息,添加订单对账单:{}", JsonUtils.toJsonString(message));
          AddReconciliationOrderDetailBO bo = new AddReconciliationOrderDetailBO();
          bo.setOrderId(message.getOrderId());
          bo.setToOrderStatus(OrderStatusEnum.WAIT_PAY);
          bo.setUserStockFlowEventType(UserStockFlowEventTypeEnum.ORDER_OUTBOUND);
          reconciliationOrderDetailService.addReconciliationOrderDetail(bo);


     }




}