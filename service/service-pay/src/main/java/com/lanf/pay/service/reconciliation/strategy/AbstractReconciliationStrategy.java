package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.model.enums.ReconciliationTradeStatusEnum;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    protected abstract ReconciliationJobTypeEnum getJobType();
    protected abstract ReconciliationScanPageResult<T> doPage(ReconciliationScanPage page);

    protected abstract List<ReconciliationTradeInfo> buildTradeInfoList(List<T> dataList);

    protected abstract ReconciliationDiffTypeEnum getDiffType();

    protected abstract ReconciliationBusinessTypeEnum getBusinessType();

    protected abstract ReconciliationTradeStatusEnum toReconciliationTradeStatus(T data);

    protected abstract Map<String,ReconciliationTradeInfo> toReconciliationTradeInfoMap(List<String> outTradeNoList);

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
            Integer count = reconciliationDiffMarkerService.lambdaQuery()
                    .eq(ReconciliationDiffMarkerDO::getBatchId, bathId)
                    .eq(ReconciliationDiffMarkerDO::getDiffType, getDiffType())
                    .eq(ReconciliationDiffMarkerDO::getBusinessType, getBusinessType())
                    .in(ReconciliationDiffMarkerDO::getBusinessOrderNo, outTradeNoList)
                    .count();

            if ( count != null && outTradeNoList.size() == count) {
                log.info("该批次已对账");
                continue;
            }

            List<ReconciliationTradeInfo> tradeInfoList = buildTradeInfoList(dataList);

            ReconciliationStartMessage reconciliationStartMessage = new ReconciliationStartMessage();
            reconciliationStartMessage.setJobType(jobType);
            reconciliationStartMessage.setReconciliationTradeInfoList(tradeInfoList);
            reconciliationStartMessage.setBathId(bathId);
            reconciliationStartMessage.setReconciliationBusinessType(getBusinessType());
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


    @Override
    public void startReconciliation(ReconciliationStart start) {


        List<ReconciliationTradeInfo> reconciliationTradeInfoList =
                start.getReconciliationTradeInfoList();

        String bathId = start.getBathId();
        ReconciliationBusinessTypeEnum businessType = start.getReconciliationBusinessType();

        /**
         * 去重
         */
        List<String> outTradeNoList = reconciliationTradeInfoList.stream()
                .map(ReconciliationTradeInfo::getOutTradeNo).collect(Collectors.toList());

        Integer count = reconciliationDiffMarkerService.lambdaQuery()
                .eq(ReconciliationDiffMarkerDO::getBatchId, bathId)
                .eq(ReconciliationDiffMarkerDO::getDiffType,getDiffType())
                .eq(ReconciliationDiffMarkerDO::getBusinessType, businessType)
                .in(ReconciliationDiffMarkerDO::getBusinessOrderNo, outTradeNoList)
                .count();
        Integer size = outTradeNoList.size();
        if ( size.equals( count)){
            log.info("该批次已对账");
            return;
        }
        List<PayOrderFlowDO> list = payOrderFlowService.lambdaQuery()
                .in(PayOrderFlowDO::getOutTradeNo, outTradeNoList).list();


        Map<String, ReconciliationTradeInfo> tradeInfoMap = toReconciliationTradeInfoMap( outTradeNoList);
        /**
         *
         */
        //差异集合
        List<ReconciliationDiffDO> diffList = new ArrayList<>();
        for (ReconciliationTradeInfo reconciliationTradeInfo : reconciliationTradeInfoList){

            String outTradeNo = reconciliationTradeInfo.getOutTradeNo();
            /**
             * 1.找出长款 or短款
             */
            ReconciliationTradeInfo tradeInfo = tradeInfoMap.get(outTradeNo);
            if (tradeInfo == null){

                ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
                reconciliationDiffDO.setBatchId(bathId);
                reconciliationDiffDO.setBusinessOrderNo(outTradeNo);
                reconciliationDiffDO.setPayChannel(reconciliationTradeInfo.getPayChannel());
                reconciliationDiffDO.setDiffType(getDiffType());
                reconciliationDiffDO.setBusinessType(businessType);
                diffList.add(reconciliationDiffDO);
            } else {

                /**
                 * 2.状态比较
                 */
                //三方状态
                ReconciliationTradeStatusEnum expectedStatus = null;
                //我方状态
                ReconciliationTradeStatusEnum actualStatus = null;
                //使用账单里的时间
                Date occurTime = null;
                String payFinishTime = null;

                if (ReconciliationDiffTypeEnum.LONG.equals(getDiffType())){

                    expectedStatus = reconciliationTradeInfo.getReconciliationTradeStatus();
                    actualStatus = tradeInfo.getReconciliationTradeStatus();
                    payFinishTime = reconciliationTradeInfo.getPayFinishTime();
                } else {
                    expectedStatus = tradeInfo.getReconciliationTradeStatus();
                    actualStatus = reconciliationTradeInfo.getReconciliationTradeStatus();
                    payFinishTime = tradeInfo.getPayFinishTime();
                }
                if ( !IStringUtils.isEmpty(payFinishTime)){
                    occurTime = DateUtils.parse(payFinishTime, DateUtils.DATE_TIME);
                }
                if ( !expectedStatus.equals(actualStatus)){
                    ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
                    reconciliationDiffDO.setBatchId(bathId);
                    reconciliationDiffDO.setBusinessOrderNo(outTradeNo);
                    reconciliationDiffDO.setPayChannel(reconciliationTradeInfo.getPayChannel());
                    reconciliationDiffDO.setOccurTime(occurTime);
                    reconciliationDiffDO.setBusinessType(businessType);
                    reconciliationDiffDO.setExpectedStatus(expectedStatus);
                    reconciliationDiffDO.setActualStatus(actualStatus);
                    diffList.add(reconciliationDiffDO);
                    /**
                     * 转态不一致 不插入
                     */
                    continue;
                }

                /**
                 * 3.对比金额是否一致
                 */
                //三方金额
                BigDecimal receiptMoney = null;
                //我方金额
                BigDecimal doReceiptMoney = null;

                if (ReconciliationDiffTypeEnum.LONG.equals(getDiffType())){

                    receiptMoney = reconciliationTradeInfo.getReceiptMoney();
                    doReceiptMoney = tradeInfo.getReceiptMoney();

                } else {
                    //我方金额
                    receiptMoney = tradeInfo.getReceiptMoney();
                    doReceiptMoney = reconciliationTradeInfo.getReceiptMoney();
                }

                //可能出现负数
                BigDecimal diffAmount = BigDecimalUtils.subtract(receiptMoney, doReceiptMoney);

                int compareTo = BigDecimalUtils.compareTo(receiptMoney, doReceiptMoney);
                if (compareTo != 0) {
                    //金额不一致
                    ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
                    reconciliationDiffDO.setBatchId(bathId);
                    reconciliationDiffDO.setBusinessOrderNo(outTradeNo);
                    reconciliationDiffDO.setPayChannel(reconciliationTradeInfo.getPayChannel());
                    reconciliationDiffDO.setExpectedAmount(doReceiptMoney);
                    reconciliationDiffDO.setActualAmount(receiptMoney);
                    reconciliationDiffDO.setDiffAmount(diffAmount);
                    reconciliationDiffDO.setDiffType(ReconciliationDiffTypeEnum.AMOUNT_MISMATCH);
                    reconciliationDiffDO.setOccurTime(occurTime);
                    reconciliationDiffDO.setBusinessType(businessType);
                    diffList.add(reconciliationDiffDO);
                }



            }

        }

    }
}
