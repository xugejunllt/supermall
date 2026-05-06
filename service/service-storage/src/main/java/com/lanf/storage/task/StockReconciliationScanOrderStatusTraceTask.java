package com.lanf.storage.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.model.bo.ReconciliationOrderDetailBO;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.mq.constant.StorageMqTopicName;
import com.lanf.storage.mq.message.ShortStockReconciliation;
import com.lanf.storage.mq.message.ShortStockReconciliationMessage;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Component
public class StockReconciliationScanOrderStatusTraceTask {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;

    /**
     * 每日 9 点 扫描订单轨迹
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void scanOrderStatusTraceTask() {
        String bathId = getBathId();

        long pageNum = 1;
        long pageSize = 100;
        Page<ReconciliationOrderDetailDO> page;

        do {
            LambdaQueryWrapper<ReconciliationOrderDetailDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ReconciliationOrderDetailDO::getBathId, bathId)
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
                message.setBathId(bathId);
                message.setReconciliationList(reconciliationList);
                rocketMqClient.sendMessage(StorageMqTopicName.SHORT_STOCK_RECONCILIATION_TOPIC,
                        JsonUtils.toJsonString(message));
            }
            pageNum++;
        } while (page.getCurrent() < page.getPages());
    }

    private String getBathId() {
        return DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
    }


}
