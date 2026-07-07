package com.lanf.storage.mq.listener.event;

import com.lanf.api.order.mq.message.OrderOutBoundedMessage;
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

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = StorageMqGroupName.ORDER_OUT_BOUNDED_EVENT_ADD_RECONCILIATION_ORDER_GROUP,
        topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        selectorExpression = OrderTopicWithTag.TAG_OUTBOUNDED
)
public class OrderOutBoundedEventListener implements RocketMQListener<OrderOutBoundedMessage> {

    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;
    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(OrderOutBoundedMessage message) {
        log.info("订单出库成功消息:{}", message);

        AddReconciliationOrderDetailBO bo = new AddReconciliationOrderDetailBO();
        bo.setOrderId(message.getOrderId());
        bo.setToOrderStatus(OrderStatusEnum.OUTBOUNDED);
        bo.setUserStockFlowEventType(UserStockFlowEventTypeEnum.ORDER_OUTBOUND);
        reconciliationOrderDetailService.addReconciliationOrderDetail(bo);



    }
}
