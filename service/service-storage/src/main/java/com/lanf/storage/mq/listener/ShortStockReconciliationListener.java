package com.lanf.storage.mq.listener;

import com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.storage.StockFlowTypeEnum;
import com.lanf.constant.utils.IdUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mapper.ReconciliationDiffMapper;
import com.lanf.storage.model.bo.ReconciliationOrderDetailBO;
import com.lanf.storage.model.entity.ReconciliationDiffDO;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.storage.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.storage.model.enums.ReconciliationJobTypeEnum;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.constant.StorageMqTopicName;
import com.lanf.storage.mq.message.ShortStockReconciliation;
import com.lanf.storage.mq.message.ShortStockReconciliationMessage;
import com.lanf.storage.service.reconciliation.IReconciliationDiffService;
import com.lanf.storage.service.stock.IStockFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 短款对账
 *
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
     @Autowired
     private IReconciliationDiffService reconciliationDiffService;
     @Autowired
     private ReconciliationDiffMapper reconciliationDiffMapper;

     @Override
     public void onMessage(ShortStockReconciliationMessage message) {

          log.info("短款对账开始:{}", JsonUtils.toJsonString(message));
          String bathId = message.getBathId();
          List<ShortStockReconciliation> reconciliationList = message.getReconciliationList();

          Map<Long, ShortStockReconciliation> reconciliationMap = convertToReconciliationMap(reconciliationList);

          Map<String, StockFlowDO> stockFlowDOMap = buildstockFlowDOMap(reconciliationList);
          Map<String, ReconciliationOrderDetailBO> orderItemMap = convertToOrderItemsMap(reconciliationList);
          Map<String, ReconciliationOrderDetailBO> userStockFlowMap = convertToOrderStockFlowsMap(reconciliationList);
          List<ReconciliationDiffDO> reconciliationDiffDOList = new ArrayList<>();



          Set<Map.Entry<String, ReconciliationOrderDetailBO>> entriedSet = orderItemMap.entrySet();
          for (Map.Entry<String, ReconciliationOrderDetailBO> entry : entriedSet) {

               String entryKey = entry.getKey();
               ReconciliationOrderDetailBO item = entry.getValue();
               ReconciliationOrderDetailBO userFlow = userStockFlowMap.get(entryKey);
               Long orderId = Long.parseLong(entryKey.split("_")[0]);

               //1.用户库存流水是否缺失（订单项目有，流水没有）
               if ( userFlow == null){
                    ReconciliationDiffDO reconciliationDiffDO = getReconciliationDiffDO(bathId,
                            orderId, ReconciliationDiffTypeEnum.USER_STOCK_FLOW_MISSING,item);
                    reconciliationDiffDOList.add(reconciliationDiffDO);
                    //下面比较都要基于 userFlow 所以结算本次循环
                    continue;
               }
               //2.订单项目与用户库存流水数量是否一致
               Integer quantity = item.getQuantity();
               Integer userFlowQuantity = userFlow.getQuantity();
               if (!quantity.equals(userFlowQuantity)) {
                    ReconciliationDiffDO reconciliationDiffDO = getReconciliationDiffDO(bathId,
                            orderId, ReconciliationDiffTypeEnum.ORDER_ITEM_STOCK_FLOW_MISMATCH,item);
                    reconciliationDiffDOList.add(reconciliationDiffDO);
                    continue;
               }

               ShortStockReconciliation reconciliation = reconciliationMap.get(orderId);
               if (ReconciliationOrderStatusEnum.OUTBOUNDED.equals(reconciliation.getOrderStatus()) ){

                    StockFlowDO stockFlowDO = stockFlowDOMap.get(entryKey);
                    if ( stockFlowDO == null){
                         //3.找出短款
                         ReconciliationDiffDO reconciliationDiffDO = getReconciliationDiffDO(bathId,
                                 orderId, ReconciliationDiffTypeEnum.SHORT_STOCK,item);
                         reconciliationDiffDOList.add(reconciliationDiffDO);

                    } else {
                         //4.仓储流水数量与用户库存流水数量是否一致
                         Integer changeQuantity = stockFlowDO.getChangeQuantity();
                         if ( !userFlowQuantity.equals(changeQuantity)){
                              ReconciliationDiffDO reconciliationDiffDO = getReconciliationDiffDO(bathId,
                                      orderId, ReconciliationDiffTypeEnum.WAREHOUSE_STOCK_FLOW_MISMATCH,item);
                              reconciliationDiffDOList.add(reconciliationDiffDO);
                         }


                    }

               }


          }
          reconciliationDiffDOList.forEach(a->{

               a.setId(IdUtils.generateId());
          });
          reconciliationDiffMapper.batchInsertIgnore(reconciliationDiffDOList);

     }


     private static ReconciliationDiffDO getReconciliationDiffDO(String bathId, Long orderId,
                                                                 ReconciliationDiffTypeEnum diffType,
                                                                 ReconciliationOrderDetailBO detailBO) {
          ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
          reconciliationDiffDO.setBathId(bathId);
          reconciliationDiffDO.setOrderId(orderId);
          reconciliationDiffDO.setSkuCode(detailBO.getSkuCode());
          reconciliationDiffDO.setWarehouseId(detailBO.getWarehouseId());
          reconciliationDiffDO.setJobType(ReconciliationJobTypeEnum.SHORT_STOCK_SCAN);
          reconciliationDiffDO.setDiffType(diffType);
          return reconciliationDiffDO;
     }

     /**
      * 将ShortStockReconciliation列表转换为以orderId为key的Map
      * Key: orderId
      * Value: ShortStockReconciliation对象
      */
     private Map<Long, ShortStockReconciliation> convertToReconciliationMap(List<ShortStockReconciliation> reconciliationList) {

          return reconciliationList.stream()
                  .collect(Collectors.toMap(
                          ShortStockReconciliation::getOrderId,
                          reconciliation -> reconciliation,
                          (existing, replacement) -> existing
                  ));
     }
     private  Map<String, StockFlowDO> buildstockFlowDOMap(List<ShortStockReconciliation> reconciliationList){
          /**
           * 已出库的订单
           */
          List<ShortStockReconciliation> outboundedList = filterOutboundedList(reconciliationList);
          Map<String, StockFlowDO> stockFlowDOMap = new HashMap<>(outboundedList.size());
          if ( !IStringUtils.isEmpty(outboundedList)){
               //1.找出短款
               List<Long> orderIdList = outboundedList.stream().map(ShortStockReconciliation::getOrderId)
                       .collect(Collectors.toList());

               List<StockFlowDO> list = stockFlowService.lambdaQuery()
                       .eq(StockFlowDO::getFlowType, StockFlowTypeEnum.SALES_OUTBOUND)
                       .in(StockFlowDO::getStockId,orderIdList)
                       .list();
               stockFlowDOMap = convertToStockFlowMap(list);

          }
          return stockFlowDOMap;
     }

     /**
      * 将ShortStockReconciliation列表中的订单项转换为Map
      * Key: orderId_skuCode_warehouseId（联合键）
      * Value: ReconciliationOrderDetailBO对象
      */
     private Map<String, ReconciliationOrderDetailBO> convertToOrderItemsMap(List<ShortStockReconciliation> reconciliationList) {


          Map<String, ReconciliationOrderDetailBO> resultMap = new HashMap<>();

          for (ShortStockReconciliation reconciliation : reconciliationList) {
               Long orderId = reconciliation.getOrderId();
               List<ReconciliationOrderDetailBO> orderItems = reconciliation.getOrderItems();

               if (orderItems != null && !orderItems.isEmpty()) {
                    for (ReconciliationOrderDetailBO item : orderItems) {
                         String key = buildKey(orderId, item.getSkuCode(), item.getWarehouseId());
                         resultMap.put(key, item);
                    }
               }
          }

          return resultMap;
     }
     /**
      * 将ShortStockReconciliation列表中的订单项转换为Map
      * Key: orderId_skuCode_warehouseId（联合键）
      * Value: ReconciliationOrderDetailBO对象
      */
     private Map<String, ReconciliationOrderDetailBO> convertToOrderStockFlowsMap(List<ShortStockReconciliation> reconciliationList) {


          Map<String, ReconciliationOrderDetailBO> resultMap = new HashMap<>();

          for (ShortStockReconciliation reconciliation : reconciliationList) {
               Long orderId = reconciliation.getOrderId();
               List<ReconciliationOrderDetailBO> stockFlows = reconciliation.getStockFlows();

               if (stockFlows != null && !stockFlows.isEmpty()) {
                    for (ReconciliationOrderDetailBO item : stockFlows) {
                         String key = buildKey(orderId, item.getSkuCode(), item.getWarehouseId());
                         resultMap.put(key, item);
                    }
               }
          }

          return resultMap;
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
                          flow -> buildKey(flow.getOrderId(), flow.getSkuCode(), flow.getWarehouseId()),
                          flow -> flow,
                          (existing, replacement) -> existing
                  ));
     }

     /**
      * 构建库存流水的唯一键
      */
     private String buildKey(Long orderId, String skuCode, Long warehouseId) {
          return orderId + "_" + skuCode + "_" + warehouseId;
     }

}