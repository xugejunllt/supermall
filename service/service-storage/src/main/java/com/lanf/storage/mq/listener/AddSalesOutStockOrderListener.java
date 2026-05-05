package com.lanf.storage.mq.listener;


import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.constant.enums.FlowNoPrefixEnum;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.message.AddSalesOutStockOrderMessage;
import com.lanf.order.mq.message.InOutStockOrderItem;
import com.lanf.storage.model.entity.InOutStockOrderItemDO;
import com.lanf.storage.model.entity.SalesOutStockOrderDO;
import com.lanf.storage.model.enums.StorageStatusEnum;
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
@RocketMQMessageListener(topic = OrderClientTopicName.ADD_SALES_OUT_STOCK_ORDER_TOPIC,
        consumerGroup = StorageMqGroupName.ADD_SALES_OUT_STOCK_ORDER_GROUP)

public class AddSalesOutStockOrderListener implements RocketMQListener<AddSalesOutStockOrderMessage> {

    @Autowired
    private ISalesOutStockOrderService salesOutStockOrderService;
    @Autowired
    private IInOutStockOrderItemService iInOutStockOrderItemService;

    @Transactional
    @Override
    public void onMessage(AddSalesOutStockOrderMessage message) {


        SalesOutStockOrderDO one = salesOutStockOrderService.lambdaQuery()
                .eq(SalesOutStockOrderDO::getOrderId, message.getOrderId())
                .one();
        if (one != null) {
            log.warn("销售出库单已存在");
        }

        SalesOutStockOrderDO stockOrderDO = new SalesOutStockOrderDO();
        stockOrderDO.setOrderId(message.getOrderId());
        stockOrderDO.setCode(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.SALES_OUTBOUND_ORDER));
        stockOrderDO.setStorageStatus(StorageStatusEnum.WAIT_OUTBOUND);
        stockOrderDO.setId(IdUtils.generateId());

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
        return inOutStockOrderItemDO;
    }

}
