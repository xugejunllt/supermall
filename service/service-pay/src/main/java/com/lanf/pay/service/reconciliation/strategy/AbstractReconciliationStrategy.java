package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.common.utils.IStringUtils;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.bo.SendMessageAndUpdateResult;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 对账扫描策略抽象基类
 */
@Slf4j
public abstract class AbstractReconciliationStrategy<T> implements ReconciliationStrategy {

    @Autowired
    private  IPayOrderFlowService payOrderFlowService;
    @Autowired
    private  IReconciliationDiffMarkerService reconciliationDiffMarkerService;
    @Autowired
    private  RocketMqClient rocketMqClient;
    @Autowired
    private  IReconciliationJobLogService reconciliationJobLogService;



    protected abstract ReconciliationScanPageResult<T> doPage(ReconciliationScanPage page);

    protected abstract List<ReconciliationTradeInfo> buildTradeInfoList(List<T> dataList);


    @Override
    public void startScan(String bathId) {

        ReconciliationJobTypeEnum jobType = getJobType();

        long currentPage = 1;
        long pageSize = 100;

        while (true) {

            /**
             * 分页查询
             */
            ReconciliationScanPage pages = new ReconciliationScanPage();
            pages.setCurrentPage(currentPage);
            pages.setPageSize(pageSize);
            ReconciliationScanPageResult<T> resultPage = doPage(pages);
            List<String> outTradeNoList = resultPage.getOutTradeNoList();
            List<T> dataList = resultPage.getDataList();
            if (IStringUtils.isEmpty(dataList)){

                return;
            }
            /**
             * 去重
             */
            List<ReconciliationDiffMarkerDO> list = reconciliationDiffMarkerService.lambdaQuery()
                    .in(ReconciliationDiffMarkerDO::getBusinessOrderNo, outTradeNoList).list();

            if (outTradeNoList.size() == list.size()) {
                log.info("该批次已对账");
                continue;
            }

            List<ReconciliationTradeInfo> tradeInfoList = buildTradeInfoList(dataList);

            ReconciliationStartMessage reconciliationStartMessage = new ReconciliationStartMessage();
            reconciliationStartMessage.setJobType(jobType);
            reconciliationStartMessage.setReconciliationTradeInfoList(tradeInfoList);

            try {

                SendMessageAndUpdateResult result = reconciliationJobLogService.sendMessageAndUpdate(reconciliationStartMessage, jobType,
                        bathId, currentPage, resultPage.getPages());
                if (result.getToBreak()){
                    break;
                }

            } catch (Exception e) {
                /**
                 * 这批次处理 即使出现异常  也要继续处理下一批次
                 */
               log.info("批次号 {} 批次 {} 扫描任务异常", bathId, jobType);
            }

            currentPage++;
        }

        log.info("批次号 {} 交易单长款扫描完成", bathId);
    }
}
