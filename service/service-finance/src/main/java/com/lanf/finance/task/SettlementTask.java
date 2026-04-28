package com.lanf.finance.task;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.aftersales.api.AfterSalesOrderApiService;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.finance.model.enums.LiquidationStatusEnum;
import com.lanf.finance.service.ILiquidationService;
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
    private ILiquidationService liquidationService ;

    private static final int PAGE_SIZE = 2000;

    @Scheduled(cron = "0 0 20 ? * ? ")
    public void settlementTask() {
        log.info("开始执行结算任务");
        
        long currentPage = 1;
        int totalProcessed = 0;
        
        while (true) {
            Page<LiquidationDO> page = new Page<>(currentPage, PAGE_SIZE);
            
            LambdaQueryWrapper<LiquidationDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LiquidationDO::getStatus, LiquidationStatusEnum.WAIT_SETTLEMENT)
                    .le(LiquidationDO::getAfterSaleExpireTime, new Date())
                    .orderByAsc(LiquidationDO::getId);
            
            Page<LiquidationDO> resultPage = liquidationService.page(page, queryWrapper);
            List<LiquidationDO> liquidationList = resultPage.getRecords();
            
            if ( liquidationList.isEmpty()) {
                break;
            }
            
            for (LiquidationDO liquidation : liquidationList) {
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

    private void processSettlement(LiquidationDO liquidation) {
        log.info("处理清算单结算，订单ID: {}", liquidation.getOrderId());
    }


}
