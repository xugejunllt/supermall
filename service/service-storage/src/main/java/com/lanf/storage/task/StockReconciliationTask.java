package com.lanf.storage.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.storage.StockFlowTypeEnum;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import com.lanf.storage.model.bo.ReconciliationOrderDetailBO;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.storage.mq.constant.StorageMqTopicName;
import com.lanf.storage.mq.message.LongStockReconciliation;
import com.lanf.storage.mq.message.LongStockReconciliationMessage;
import com.lanf.storage.mq.message.ShortStockReconciliation;
import com.lanf.storage.mq.message.ShortStockReconciliationMessage;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import com.lanf.storage.service.stock.IStockFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存对账
 */
@Slf4j
@Component
public class StockReconciliationTask {

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;
    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;
    @Autowired
    private IStockFlowService stockFlowService;


    /**
     * 每日 9 点 扫描
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void shortStockReconciliationScanTask() {

        String batchId = BatchIdContext.getBatchId();
        if (batchId == null) {
            batchId = getBathId();
        }

        long pageNum = 1;
        long pageSize = 100;
        Page<ReconciliationOrderDetailDO> page;

        try {
            do {
                LambdaQueryWrapper<ReconciliationOrderDetailDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ReconciliationOrderDetailDO::getBathId, batchId)
                        .orderByAsc(ReconciliationOrderDetailDO::getId);

                page = reconciliationOrderDetailService.page(new Page<>(pageNum, pageSize), queryWrapper);

                List<ReconciliationOrderDetailDO> detailList = page.getRecords();
                if (!IStringUtils.isEmpty(detailList)) {

                    List<ShortStockReconciliation> reconciliationList = detailList.stream().map(a -> {
                        ShortStockReconciliation message = new ShortStockReconciliation();

                        message.setOrderId(a.getOrderId());
                        message.setOrderStatus(a.getOrderStatus());
                        if (!IStringUtils.isEmpty(a.getOrderItems())) {
                            message.setOrderItems(JsonUtils.toList(a.getOrderItems(),
                                    ReconciliationOrderDetailBO.class));
                        }
                        if (!IStringUtils.isEmpty(a.getStockFlows())) {
                            message.setStockFlows(JsonUtils.toList(a.getStockFlows(),
                                    ReconciliationOrderDetailBO.class));

                        }
                        return message;

                    }).collect(Collectors.toList());
                    ShortStockReconciliationMessage message = new ShortStockReconciliationMessage();
                    message.setBathId(batchId);
                    message.setReconciliationList(reconciliationList);
                    mqSendMessageUtils.sendMessage(StorageMqTopicName.SHORT_STOCK_RECONCILIATION_TOPIC,
                            JsonUtils.toJsonString(message),null);
                }
                pageNum++;
            } while (page.getCurrent() < page.getPages());
        } finally {
            BatchIdContext.clear();
        }
    }

    private String getBathId() {
        return DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
    }

    /**
     * 每日 9 点 扫描
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void longStockReconciliationScanTask() {

        String batchId = BatchIdContext.getBatchId();
        if (batchId == null) {
            batchId = getBathId();
        }
        long pageNum = 1;
        long pageSize = 100;
        Page<StockFlowDO> page;

        try {
            do {
                LambdaQueryWrapper<StockFlowDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StockFlowDO::getCreateDate, batchId)
                        .eq(StockFlowDO::getFlowType, StockFlowTypeEnum.SALES_OUTBOUND)
                        .orderByAsc(StockFlowDO::getId);

                page = stockFlowService.page(new Page<>(pageNum, pageSize), queryWrapper);

                List<StockFlowDO> stockFlowList = page.getRecords();
                if (!IStringUtils.isEmpty(stockFlowList)) {

                    List<LongStockReconciliation> reconciliationList = stockFlowList.stream().map(a -> {
                        LongStockReconciliation reconciliation = new LongStockReconciliation();
                        reconciliation.setOrderId(a.getOrderId());
                        reconciliation.setSkuCode(a.getSkuCode());
                        reconciliation.setQuantity(a.getChangeQuantity());
                        reconciliation.setWarehouseId(a.getWarehouseId());
                        reconciliation.setStockFlowId(a.getId());
                        return reconciliation;
                    }).collect(Collectors.toList());
                    LongStockReconciliationMessage message = new LongStockReconciliationMessage();
                    message.setBathId(batchId);
                    message.setReconciliationList(reconciliationList);
                    mqSendMessageUtils.sendMessage(StorageMqTopicName.LONG_STOCK_RECONCILIATION_TOPIC,
                            JsonUtils.toJsonString(message),null);
                }

                pageNum++;
            } while (page.getCurrent() < page.getPages());
        } finally {
            BatchIdContext.clear();
        }
    }

}
