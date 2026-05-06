package com.lanf.goods.mq.listener;


import com.lanf.common.utils.JsonUtils;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.model.enums.UserStockFlowEventTypeEnum;
import com.lanf.goods.mq.constant.GoodsMqGroupName;

import com.lanf.goods.service.goods.IUserStockFlowService;
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
        consumerGroup = GoodsMqGroupName.QUERY_STOCK_FLOW_GROUP)
public class QueryStockFlowListener implements RocketMQListener<ReconciliationOrderSaveSuccessNotifyMessage> {

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;


    @Override
    public void onMessage(ReconciliationOrderSaveSuccessNotifyMessage message) {

        List<ReconciliationOrderDetail> orderDetails = message.getOrderDetails();

        List<Long> orderIdList = orderDetails.stream()
                .map(ReconciliationOrderDetail::getOrderId)
                .collect(Collectors.toList());

        List<UserStockFlowDO> orderItemDOList = userStockFlowService.lambdaQuery()
                .in(UserStockFlowDO::getOrderId, orderIdList)
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
    private List<ReconciliationOrderDetailItem> convertToReconciliationOrderDetailItems(List<UserStockFlowDO> orderItemList) {

        return orderItemList.stream().map(orderItem -> {
            ReconciliationOrderDetailItem item = new ReconciliationOrderDetailItem();
            item.setOrderId(orderItem.getOrderId());
            item.setQuantity(orderItem.getChangeQuantity());
            item.setSkuCode(orderItem.getSkuCode());
            item.setWarehouseId(orderItem.getWarehouseId());
            item.setEventType(UserStockFlowEventTypeEnum.getByCode(orderItem.getEventType()));
            return item;
        }).collect(Collectors.toList());
    }


}
