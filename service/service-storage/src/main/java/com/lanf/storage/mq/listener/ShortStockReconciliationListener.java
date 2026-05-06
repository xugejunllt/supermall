package com.lanf.storage.mq.listener;

import com.lanf.common.utils.IStringUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.storage.model.enums.StockFlowTypeEnum;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.constant.StorageMqTopicName;
import com.lanf.storage.mq.message.ShortStockReconciliation;
import com.lanf.storage.mq.message.ShortStockReconciliationMessage;
import com.lanf.storage.service.stock.IStockFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 监听订单创建成功事件添加订单对账单
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageMqTopicName.SHORT_STOCK_RECONCILIATION_TOPIC,
        consumerGroup = StorageMqGroupName.SHORT_STOCK_RECONCILIATION_GROUP)
public class ShortStockReconciliationListener implements RocketMQListener<ShortStockReconciliationMessage> {

     @Autowired
     private RocketMqClient rocketMqClient;
     @Autowired
     private IStockFlowService stockFlowService;



     @Override
     public void onMessage(ShortStockReconciliationMessage message) {

          List<ShortStockReconciliation> reconciliationList = message.getReconciliationList();
          /**
           * 已出库的订单
           */
          List<ShortStockReconciliation> outboundedList = filterOutboundedList(reconciliationList);
          if ( !IStringUtils.isEmpty(outboundedList)){
               //1.找出短款
               List<Long> orderIdList = outboundedList.stream().map(ShortStockReconciliation::getOrderId)
                       .collect(Collectors.toList());

               List<StockFlowDO> list = stockFlowService.lambdaQuery()
                       .eq(StockFlowDO::getFlowType, StockFlowTypeEnum.SALES_OUTBOUND)
                       .in(StockFlowDO::getStockId).list();
               Map<String, StockFlowDO> stringStockFlowDOMap = convertToStockFlowMap(list);


          }





     }

     /**
      * 过滤出订单状态为已出库（OUTBOUNDED）的列表
      */
     private List<ShortStockReconciliation> filterOutboundedList(List<ShortStockReconciliation> reconciliationList) {

          
          return reconciliationList.stream()
                  .filter(item -> ReconciliationOrderStatusEnum.OUTBOUNDED.equals(item.getOrderStatus()))
                  .collect(Collectors.toList());
     }

     /**
      * 将StockFlowDO列表转换为Map
      * Key: orderId_skuCode_warehouseId（联合键）
      * Value: StockFlowDO对象
      */
     private Map<String, StockFlowDO> convertToStockFlowMap(List<StockFlowDO> stockFlowList) {

          
          return stockFlowList.stream()
                  .collect(Collectors.toMap(
                          flow -> buildStockFlowKey(flow.getOrderId(), flow.getSkuCode(), flow.getWarehouseId()),
                          flow -> flow,
                          (existing, replacement) -> existing
                  ));
     }

     /**
      * 构建库存流水的唯一键
      */
     private String buildStockFlowKey(Long orderId, String skuCode, Long warehouseId) {
          return orderId + "_" + skuCode + "_" + warehouseId;
     }

}