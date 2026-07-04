package com.lanf.storage.mq.listener;

/**
 * 长库存对账任务
 */

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.utils.IdUtils;
import com.lanf.storage.mapper.ReconciliationDiffMapper;
import com.lanf.storage.model.bo.ReconciliationOrderDetailBO;
import com.lanf.storage.model.entity.ReconciliationDiffDO;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.storage.model.enums.ReconciliationJobTypeEnum;
import com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.constant.StorageMqTopicName;
import com.lanf.storage.mq.message.LongStockReconciliation;
import com.lanf.storage.mq.message.LongStockReconciliationMessage;
import com.lanf.storage.service.reconciliation.IReconciliationDiffService;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageMqTopicName.LONG_STOCK_RECONCILIATION_TOPIC, consumerGroup =
        StorageMqGroupName.LONG_STOCK_RECONCILIATION_GROUP)
public class LongStockReconciliationListener implements RocketMQListener<LongStockReconciliationMessage> {


    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;
    @Autowired
    private IReconciliationDiffService reconciliationDiffService;
    @Autowired
    private ReconciliationDiffMapper reconciliationDiffMapper;

    @Override
    public void onMessage(LongStockReconciliationMessage message) {


        String bathId = message.getBathId();
        List<LongStockReconciliation> reconciliationList = message.getReconciliationList();


        List<Long> orderIdList = reconciliationList.stream().map(LongStockReconciliation::getOrderId)
                .collect(Collectors.toList());

        List<ReconciliationOrderDetailDO> detailDOList = reconciliationOrderDetailService.lambdaQuery()
                .eq(ReconciliationOrderDetailDO::getOrderStatus, ReconciliationOrderStatusEnum.OUTBOUNDED)
                .in(ReconciliationOrderDetailDO::getOrderId, orderIdList)
                .list();
        Map<String, ReconciliationOrderDetailBO> orderDetailBOMap =
                new HashMap<>(detailDOList.size()*10);

        for (ReconciliationOrderDetailDO detailDO : detailDOList){

            String stockFlows = detailDO.getStockFlows();
            Long orderId = detailDO.getOrderId();
            List<ReconciliationOrderDetailBO> detailBOS = JsonUtils.toList(stockFlows, ReconciliationOrderDetailBO.class);

           for (ReconciliationOrderDetailBO detailBO : detailBOS){
               String buildKey = buildKey(orderId, detailBO.getSkuCode(), detailBO.getWarehouseId());
               orderDetailBOMap.put(buildKey, detailBO);

           }


        }
        List<ReconciliationDiffDO> reconciliationDiffDOList = new ArrayList<>();
        for (LongStockReconciliation item : reconciliationList){
            String buildKey = buildKey(item.getOrderId(), item.getSkuCode(), item.getWarehouseId());
            ReconciliationOrderDetailBO orderDetailBO = orderDetailBOMap.get(buildKey);
            if (orderDetailBO == null){
                ReconciliationDiffDO diffDO = getReconciliationDiffDO(bathId, item.getStockFlowId(),
                        ReconciliationDiffTypeEnum.LONG_STOCK,
                        item);
                reconciliationDiffDOList.add(diffDO);
            }
        }
        reconciliationDiffDOList.forEach(a->{

            a.setId(IdUtils.generateId());
        });
        reconciliationDiffMapper.batchInsertIgnore(reconciliationDiffDOList);
    }


    /**
     * 构建库存流水的唯一键
     */
    private String buildKey(Long orderId, String skuCode, Long warehouseId) {
        return orderId + "_" + skuCode + "_" + warehouseId;
    }
    private static ReconciliationDiffDO getReconciliationDiffDO(String bathId, Long flowId,
                                                                ReconciliationDiffTypeEnum diffType,
                                                                LongStockReconciliation detailBO) {
        ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
        reconciliationDiffDO.setBathId(bathId);
        reconciliationDiffDO.setStockFlowId(flowId);
        reconciliationDiffDO.setSkuCode(detailBO.getSkuCode());
        reconciliationDiffDO.setWarehouseId(detailBO.getWarehouseId());
        reconciliationDiffDO.setJobType(ReconciliationJobTypeEnum.LONG_STOCK_SCAN);
        reconciliationDiffDO.setDiffType(diffType);
        return reconciliationDiffDO;
    }
}
