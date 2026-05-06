package com.lanf.order.mq.listener.event;


import com.lanf.common.utils.JsonUtils;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.IOrderItemService;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.message.ReconciliationOrderDetail;
import com.lanf.storage.mq.message.ReconciliationOrderDetailItem;
import com.lanf.storage.mq.message.ReconciliationOrderSaveSuccessNotifyMessage;
import com.lanf.storage.mq.message.UpdateReconciliationOrderDetailItemMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 插入ReconciliationOrderDetail成功后 查询订单item
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = StorageClientTopicName.RECONCILIATION_ORDER_SAVE_SUCCESS_NOTIFY_TOPIC,
        consumerGroup = OrderMqGroupName.QUERY_ORDER_ITEM_GROUP)
public class QueryOrderItemListener implements RocketMQListener<ReconciliationOrderSaveSuccessNotifyMessage> {

    @Autowired
    private IOrderItemService orderItemService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(ReconciliationOrderSaveSuccessNotifyMessage message) {

        List<ReconciliationOrderDetail> orderDetails = message.getOrderDetails();

        List<Long> orderIdList = orderDetails.stream()
                .map(ReconciliationOrderDetail::getOrderId)
                .collect(Collectors.toList());
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery().in(OrderItemDO::getOrderId, orderIdList)
                .list();
        List<ReconciliationOrderDetailItem> detailItems = convertToReconciliationOrderDetailItems(orderItemDOList);
        UpdateReconciliationOrderDetailItemMessage updateMessage = new UpdateReconciliationOrderDetailItemMessage();
        updateMessage.setBathId(message.getBathId());
        updateMessage.setMaxOrderId(message.getMaxOrderId());
        updateMessage.setOrderDetailItems(detailItems);
        updateMessage.setOrderDetails(orderDetails);


        rocketMqClient.sendMessageWithTags(StorageClientTopicName
                        .UPDATE_RECONCILIATION_ORDER_DETAIL_ITEM_TOPIC,
                StorageClientTopicName.UPDATE_STOCK_FLOW_TAG, JsonUtils.toJsonString(updateMessage));

    }

    /**
     * 将订单项列表转换为对账订单明细项列表
     */
    private List<ReconciliationOrderDetailItem> convertToReconciliationOrderDetailItems(List<OrderItemDO> orderItemList) {

        return orderItemList.stream().map(orderItem -> {
            ReconciliationOrderDetailItem item = new ReconciliationOrderDetailItem();
            item.setOrderId(orderItem.getOrderId());
            item.setQuantity(orderItem.getQuantity());
            item.setSkuCode(orderItem.getSkuCode());
            item.setWarehouseId(orderItem.getWarehouseId());
            return item;
        }).collect(Collectors.toList());
    }


}
