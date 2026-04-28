package com.lanf.finance.task;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.aftersales.api.AfterSalesOrderApiService;
import com.lanf.finance.model.entity.ClearingOrderDO;
import com.lanf.finance.model.enums.ClearingOrderStatusEnum;
import com.lanf.finance.mq.constant.FinanceMqTopicName;
import com.lanf.finance.mq.message.ClearingOrderMessage;
import com.lanf.finance.service.IClearingOrderService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SettlementTask {


    @Autowired
    private AfterSalesOrderApiService afterSalesOrderApiService;
    @Autowired
    private IClearingOrderService liquidationService ;
    @Autowired
    private RocketMqClient rocketMqClient;

    private static final int PAGE_SIZE = 2000;

    @Scheduled(cron = "0 0 20 ? * ? ")
    public void settlementTask() {
        log.info("开始执行结算任务");
        
        long currentPage = 1;
        int totalProcessed = 0;
        
        while (true) {
            Page<ClearingOrderDO> page = new Page<>(currentPage, PAGE_SIZE);
            
            LambdaQueryWrapper<ClearingOrderDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.WAIT_SETTLEMENT)
                    .le(ClearingOrderDO::getAfterSaleExpireTime, new Date())
                    .orderByAsc(ClearingOrderDO::getId);
            
            Page<ClearingOrderDO> resultPage = liquidationService.page(page, queryWrapper);
            List<ClearingOrderDO> liquidationList = resultPage.getRecords();
            
            if ( liquidationList.isEmpty()) {
                break;
            }
            
            for (ClearingOrderDO liquidation : liquidationList) {
                try {
                    processSettlement(liquidation);
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("处理清算单 {} 结算失败", liquidation.getId(), e);
                }
            }
            
            if (!resultPage.hasNext()) {
                break;
            }
            currentPage++;
        }
        
        log.info("结算任务执行完成，共处理 {} 条记录", totalProcessed);
    }

    private void processSettlement(ClearingOrderDO liquidation) {
        log.info("处理清算单结算，订单ID: {}", liquidation.getOrderId());
        ClearingOrderMessage settlementTaskMessage = new ClearingOrderMessage();
        settlementTaskMessage.setLiquidationId(liquidation.getId());
        settlementTaskMessage.setOrderId(liquidation.getOrderId());
        rocketMqClient.sendMessage(FinanceMqTopicName.SETTLEMENT_TASK_TOPIC, settlementTaskMessage);

    }


}
