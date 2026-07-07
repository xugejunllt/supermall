package com.lanf.storage.mq.listener.event;


import com.lanf.api.order.mq.message.InOutStockOrderItem;
import com.lanf.api.order.mq.message.OrderWaitOutboundMessage;
import com.lanf.api.storage.model.enums.StorageStatusEnum;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.constant.utils.IdUtils;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.storage.model.entity.InOutStockOrderItemDO;
import com.lanf.storage.model.entity.SalesOutStockOrderDO;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加销售出库单
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = StorageMqGroupName.ADD_SALES_OUT_STOCK_ORDER_GROUP,
        selectorExpression = OrderTopicWithTag.TAG_WAIT_OUTBOUND)

public class OrderWaitOutboundEventAddSalesOutStockOrderListener implements RocketMQListener<OrderWaitOutboundMessage> {

    @Autowired
    private ISalesOutStockOrderService salesOutStockOrderService;
    @Autowired
    private IInOutStockOrderItemService iInOutStockOrderItemService;
    @MqRetryConsume(messageId = "#message.messageId")
    @Transactional
    @Override
    public void onMessage(OrderWaitOutboundMessage message) {

        log.info("订单允许发货消息,添加销售出库单:{}", JsonUtils.toJsonString(message));
        SalesOutStockOrderDO one = salesOutStockOrderService.lambdaQuery()
                .eq(SalesOutStockOrderDO::getOrderId, message.getOrderId())
                .one();
        if (one != null) {
            log.warn("销售出库单已存在");
            return;
        }

        SalesOutStockOrderDO stockOrderDO = new SalesOutStockOrderDO();
        stockOrderDO.setOrderId(message.getOrderId());
        stockOrderDO.setCode(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.SALES_OUTBOUND_ORDER));
        stockOrderDO.setStorageStatus(StorageStatusEnum.WAIT_OUTBOUND);
        stockOrderDO.setId(IdUtils.generateId());
        stockOrderDO.setTenantId(message.getTenantId());
        stockOrderDO.setUserId(message.getUserId());
        List<InOutStockOrderItem> items = message.getItems();
        List<InOutStockOrderItemDO> inOutStockOrderItemDOList = new ArrayList<>(items.size());
        for (InOutStockOrderItem item : items) {
            InOutStockOrderItemDO inOutStockOrderItemDO = buildInOutStockOrderItemDO(item, stockOrderDO);
            inOutStockOrderItemDOList.add(inOutStockOrderItemDO);

        }
        try {
            salesOutStockOrderService.save(stockOrderDO);
        } catch (DuplicateKeyException e) {
            log.warn("销售出库单已存在");
            return;
        }
        iInOutStockOrderItemService.saveBatch(inOutStockOrderItemDOList);
    }


    private static InOutStockOrderItemDO buildInOutStockOrderItemDO(InOutStockOrderItem item, SalesOutStockOrderDO stockOrderDO) {
        InOutStockOrderItemDO inOutStockOrderItemDO = new InOutStockOrderItemDO();
        inOutStockOrderItemDO.setInOutStockOrderId(stockOrderDO.getId());
        inOutStockOrderItemDO.setGoodsName(item.getGoodsName());
        inOutStockOrderItemDO.setSkuCode(item.getSkuCode());
        inOutStockOrderItemDO.setTotalQuantity(item.getTotalQuantity());
        inOutStockOrderItemDO.setSurplusQuantity(item.getTotalQuantity());
        inOutStockOrderItemDO.setUnit(item.getUnit());
        inOutStockOrderItemDO.setWarehouseId(item.getWarehouseId());
        inOutStockOrderItemDO.setTenantId(stockOrderDO.getTenantId());
        return inOutStockOrderItemDO;
    }

}
