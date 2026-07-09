package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.api.pay.model.enums.*;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.utils.IdUtils;
import com.lanf.pay.mapper.ReconciliationDiffMapper;
import com.lanf.pay.mapper.ReconciliationDiffMarkerMapper;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffService;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IReconciliationDiffMarkerService reconciliationDiffMarkerService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;
    @Autowired
    private MaxIdTrackingBatchReconciler maxIdTrackingBatchReconciler;
    @Autowired
    private IReconciliationDiffService reconciliationDiffService;
    @Autowired
    private ReconciliationDiffMapper reconciliationDiffMapper;

    @Autowired
    private ReconciliationDiffMarkerMapper reconciliationDiffMarkerMapper;


    protected abstract ReconciliationScanPageResult<T> doPage(ReconciliationScanPage page);

    protected abstract List<ReconciliationTradeInfo> buildTradeInfoList(List<T> dataList);

    protected abstract ReconciliationDiffTypeEnum getDiffType();

    protected abstract ReconciliationBusinessTypeEnum getBusinessType();

    protected abstract ReconciliationTradeStatusEnum toReconciliationTradeStatus(T data);

    protected abstract Map<String, ReconciliationTradeInfo> toReconciliationTradeInfoMap(List<String> outTradeNoList);

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
            pages.setBathId(bathId);
            ReconciliationScanPageResult<T> resultPage = doPage(pages);
            List<T> dataList = resultPage.getDataList();
            if (IStringUtils.isEmpty(dataList)) {
                reconciliationJobLogService.lambdaUpdate()
                        .eq(ReconciliationJobLogDO::getBatchId, bathId)
                        .eq(ReconciliationJobLogDO::getJobType, jobType)
                        .set(ReconciliationJobLogDO::getStatus, ReconciliationJobStatusEnum.SCAN_COMPLETED)
                        .update();
                log.warn("数据为空");
                return;
            }

            List<ReconciliationTradeInfo> tradeInfoList = buildTradeInfoList(dataList);
            /**
             * tradeInfoList 是根据id升序 所以取集合最后一个
             */
            ReconciliationTradeInfo tradeInfo = tradeInfoList.get(tradeInfoList.size() - 1);
            /**
             * 去重
             */

            boolean saveBath = maxIdTrackingBatchReconciler.isSaveBath(bathId, getDiffType(),
                    getBusinessType(), tradeInfo.getId());
            if (saveBath) {
                currentPage++;
                log.info("该批次已对账");
                continue;
            }

            ReconciliationStartMessage reconciliationStartMessage = new ReconciliationStartMessage();
            reconciliationStartMessage.setJobType(jobType);
            reconciliationStartMessage.setReconciliationTradeInfoList(tradeInfoList);
            reconciliationStartMessage.setBathId(bathId);
            reconciliationStartMessage.setReconciliationBusinessType(getBusinessType());
            reconciliationStartMessage.setBathMaxId(tradeInfo.getId());
            try {

                SendMessageAndUpdateResult result = reconciliationJobLogService.sendMessageAndUpdate(reconciliationStartMessage, jobType,
                        bathId, currentPage, resultPage.getPages());
                if (result.getToBreak()) {
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

    @Transactional
    @Override
    public void startReconciliation(ReconciliationStart start) {

        String bathId = start.getBathId();
        ReconciliationBusinessTypeEnum businessType = start.getReconciliationBusinessType();
        boolean saveBath = maxIdTrackingBatchReconciler.isSaveBath(bathId, getDiffType(),
                getBusinessType(), start.getBathMaxId());
        if (saveBath) {
            log.info("该批次已对账");
            return;
        }

        List<ReconciliationTradeInfo> reconciliationTradeInfoList =
                start.getReconciliationTradeInfoList();


        /**
         * 去重
         */
        List<String> outTradeNoList = reconciliationTradeInfoList.stream()
                .map(ReconciliationTradeInfo::getOutTradeNo).collect(Collectors.toList());


        Map<String, ReconciliationTradeInfo> tradeInfoMap = toReconciliationTradeInfoMap(outTradeNoList);
        /**
         *
         */
        //差异集合
        List<ReconciliationDiffDO> diffList = new ArrayList<>();
        for (ReconciliationTradeInfo reconciliationTradeInfo : reconciliationTradeInfoList) {

            String outTradeNo = reconciliationTradeInfo.getOutTradeNo();
            /**
             * 1.找出长款 or短款
             */
            ReconciliationTradeInfo tradeInfo = tradeInfoMap.get(outTradeNo);
            if (tradeInfo == null) {

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

                if (ReconciliationDiffTypeEnum.LONG.equals(getDiffType())) {

                    expectedStatus = reconciliationTradeInfo.getReconciliationTradeStatus();
                    actualStatus = tradeInfo.getReconciliationTradeStatus();
                } else {
                    expectedStatus = tradeInfo.getReconciliationTradeStatus();
                    actualStatus = reconciliationTradeInfo.getReconciliationTradeStatus();
                }

                if (!expectedStatus.equals(actualStatus)) {
                    ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
                    reconciliationDiffDO.setBatchId(bathId);
                    reconciliationDiffDO.setBusinessOrderNo(outTradeNo);
                    reconciliationDiffDO.setPayChannel(reconciliationTradeInfo.getPayChannel());
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

                if (ReconciliationDiffTypeEnum.LONG.equals(getDiffType())) {

                    //三方金额
                    BigDecimal receiptMoney = null;
                    //我方金额
                    BigDecimal doReceiptMoney = null;

                    if (ReconciliationDiffTypeEnum.LONG.equals(getDiffType())) {

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
                        reconciliationDiffDO.setBusinessType(businessType);
                        diffList.add(reconciliationDiffDO);
                    }

                }


            }

        }
        List<ReconciliationDiffMarkerDO> diffMarkerDOList = new ArrayList<>();

        for (ReconciliationTradeInfo reconciliationTradeInfo : reconciliationTradeInfoList) {

            ReconciliationDiffMarkerDO reconciliationDiffMarkerDO = new ReconciliationDiffMarkerDO();
            reconciliationDiffMarkerDO.setBatchId(bathId);
            reconciliationDiffMarkerDO.setBusinessOrderNo(reconciliationTradeInfo.getOutTradeNo());
            reconciliationDiffMarkerDO.setDiffType(getDiffType());
            reconciliationDiffMarkerDO.setBusinessType(businessType);
            diffMarkerDOList.add(reconciliationDiffMarkerDO);
        }
        /**
         * 这里保存 系统与三方的交易单号
         */
        diffMarkerDOList.forEach(a -> {
            a.setId(IdUtils.generateId());
            Date date = new Date();
            a.setCreateTime(new Date());
            a.setUpdateTime(date);
        });
        diffList.forEach(a -> {
            a.setId(IdUtils.generateId());
            Date date = new Date();
            a.setCreateTime(new Date());
            a.setUpdateTime(date);
        });
         reconciliationDiffMapper.batchInsertIgnore(diffList);
         reconciliationDiffMarkerMapper.batchInsertIgnore(diffMarkerDOList);
         maxIdTrackingBatchReconciler.addMaxId(bathId, getDiffType(),
                getBusinessType(), start.getBathMaxId());
    }
}
