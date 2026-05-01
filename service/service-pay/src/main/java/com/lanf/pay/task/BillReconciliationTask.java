package com.lanf.pay.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.*;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * T+1对账任务
 * 多机器并发执行task
 */
@Slf4j
@Component
public class BillReconciliationTask {

    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private IChannelBillDownloadProgressService channelBillDownloadProgressService;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private IFundBillDetailService fundBillDetailService;

    @Autowired
    private IReconciliationResultService reconciliationResultService;

    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;

    @Autowired
    private IPayOrderFlowService payOrderFlowService ;


    private static final String BILL_TYPE = "signcustomer";

    /**
     * 下载账单到DB中
     * <p>
     * 使用mq 不同渠道不同mq任务处理
     * <p>
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Shanghai")
    public void billSynchronizerTask() {

        log.info("开始执行T+1下载对账单任务");

        String relativeDateString = getBathId();
        Set<PayChannelEnum> availableChannels = PayChannelEnum.AVAILABLE_CHANNELS;

        BillSynchronizerMessage billSynchronizerMessage = new BillSynchronizerMessage();

        billSynchronizerMessage.setBillType(BILL_TYPE);

        billSynchronizerMessage.setBillDate(relativeDateString);
        for (PayChannelEnum channel : availableChannels) {

            /**
             * 控制定时任务并发执行
             */
            boolean exist = channelBillDownloadProgressService.exist(relativeDateString, channel);
            if (exist) {
                log.info("{}账单已存在", channel);
            } else {
                boolean downloadProgress = channelBillDownloadProgressService.addChannelBillDownloadProgress(relativeDateString,
                        channel);
                if (!downloadProgress) {
                    log.info("{}账单正在下载中", channel);
                    continue;
                }

                String flowNo = IdUtils.generateId() + "";
                billSynchronizerMessage.setPayChannel(channel);
                billSynchronizerMessage.setFlowNo(flowNo);
                //
                rocketMqClient.sendMessage(PayMqTopicName.BILL_SYNCHRONIZER_TOPIC,
                        JsonUtils.toJsonString(billSynchronizerMessage));

            }

        }
        log.info("执行T+1定时下载对账单任务已启动");
    }

    /**
     * 每天10点之后 每隔1小时执行一次
     * 检查当前账单是否全部解析完成
     * 如果没有 进行重新投递
     */
    @Scheduled(cron = "0 0 10-23 * * ?", zone = "Asia/Shanghai")
    public void isBillParsedTask() {

        String bathId = getBathId();

        List<ChannelBillDownloadProgressDO> downloadProgressDOS =
                channelBillDownloadProgressService.lambdaQuery()
                        .eq(ChannelBillDownloadProgressDO::getBatchId, bathId).list();

        for (ChannelBillDownloadProgressDO downloadProgressDO : downloadProgressDOS) {

            BillDownloadStatusEnum status = downloadProgressDO.getStatus();

            if (BillDownloadStatusEnum.DOWNLOADING.equals(status)) {
                log.info("{}账单正在下载中,发送补投任务", downloadProgressDO.getPayChannel());

                try {

                    channelBillDownloadProgressService.redeliverTask(downloadProgressDO, BILL_TYPE);

                } catch (Exception e) {
                    log.error("发送补投任务异常", e);
                }

            }

        }


    }

    /**
     * 每天10点之后 每30分钟执行一次
     * 检查当前账单是否全部解析完成
     */
    @Scheduled(cron = "0 0/30 10-23 * * ?", zone = "Asia/Shanghai")
    public void isAllBillParsedTask() {

        String bathId = getBathId();
        List<ChannelBillDownloadProgressDO> downloadProgressDOS =
                channelBillDownloadProgressService.lambdaQuery()
                        .eq(ChannelBillDownloadProgressDO::getBatchId, bathId).list();
        boolean allCompleted = downloadProgressDOS.stream()
                .allMatch(progress -> BillDownloadStatusEnum.COMPLETED.equals(progress.getStatus()));
        if (!allCompleted) {
            log.warn("批次号 {} 的所有对账单未全部下载完成", bathId);
            return;
        }
        log.info("批次号 {} 的所有对账单已全部下载完成", bathId);
        reconciliationResultService.addReconciliationResultAndJobLog(bathId);
        /**
         * 提交扫描任务
         *
         */
        for (ReconciliationJobTypeEnum jobType : ReconciliationJobTypeEnum.values()) {
            BillScanTask billScanTask = new BillScanTask(bathId, jobType);
            taskScheduler.execute(billScanTask);
        }
        log.info("批次号 {} 的对账任务已提交", bathId);
    }

    private String getBathId(){
        return DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
    }
    /**
     * 检查扫描任务是否完成
     */
    @Scheduled(cron = "0 0 12-23 * * ?", zone = "Asia/Shanghai")
    public void billScanCompletionCheckerTask(){

        String bathId = getBathId();
        List<ReconciliationJobLogDO> jobLogDOList = reconciliationJobLogService.lambdaQuery()
                .eq(ReconciliationJobLogDO::getBatchId, bathId)
                .list();
        for (ReconciliationJobLogDO jobLogDO : jobLogDOList){

            if (jobLogDO.getStatus().isExecuting()){
                log.info("批次号 {} 的任务 {} 正在执行,重新投递", bathId, jobLogDO.getJobType());
                BillScanTask billScanTask = new BillScanTask(bathId, jobLogDO.getJobType());
                taskScheduler.execute(billScanTask);
            }

        }



    }

    static class BillScanTask implements Runnable {

        private final String bathId;
        private final ReconciliationJobTypeEnum jobType;
        private final IPayOrderFlowService payOrderFlowService;
        private final IReconciliationDiffMarkerService reconciliationDiffMarkerService;
        private final RocketMqClient rocketMqClient;
        private final  IReconciliationJobLogService reconciliationJobLogService;

        public BillScanTask(String bathId, ReconciliationJobTypeEnum jobType) {
            this.bathId = bathId;
            this.jobType = jobType;
            this.payOrderFlowService = BeanUtil.getBean(IPayOrderFlowService.class);
            this.reconciliationDiffMarkerService = BeanUtil.getBean(IReconciliationDiffMarkerService.class);
            this.rocketMqClient = BeanUtil.getBean(RocketMqClient.class);
            this.reconciliationJobLogService = BeanUtil.getBean(IReconciliationJobLogService.class);
        }

        @Override
        public void run() {

            switch (jobType){
                case TRADE_LONG_CHECK:
                    processTradeLongCheck(bathId);
                            break;
                /**
                 * 其他job类型 同样处理
                 */

            }




        }

        private void processTradeLongCheck(String bathId) {
            long currentPage = 1;
            long pageSize = 100;
            
            while (true) {

                Page<PayOrderFlowDO> page = new Page<>(currentPage, pageSize);
                /**
                 * 根据id排序
                 */
                IPage<PayOrderFlowDO> resultPage = payOrderFlowService.lambdaQuery()
                        .eq(PayOrderFlowDO::getPayFinishDate, bathId)
                        .orderByDesc(BaseEntity::getId)
                        .page(page);
                
                List<PayOrderFlowDO> orderFlowList = resultPage.getRecords();
                
                if (orderFlowList == null || orderFlowList.isEmpty()) {
                    break;
                }

                List<String> outTradeNoList = orderFlowList.stream()
                        .map(PayOrderFlowDO::getOutTradeNo).collect(Collectors.toList());

                List<ReconciliationDiffMarkerDO> list = reconciliationDiffMarkerService.lambdaQuery()
                        .in(ReconciliationDiffMarkerDO::getBusinessOrderNo, outTradeNoList).list();

                if (outTradeNoList.size() == list.size()){
                    log.info("该批次已对账");
                    continue;
                }

                List<ReconciliationTradeInfo> tradeInfoList = new ArrayList<>();
                for (PayOrderFlowDO orderFlow : orderFlowList) {

                    ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
                    tradeInfo.setOutTradeNo(orderFlow.getOutTradeNo());
                    tradeInfo.setReceiptMoney(orderFlow.getReceiptMoney());
                    tradeInfo.setPayChannel(PayChannelEnum.getByCode(orderFlow.getPayType()));
                    tradeInfo.setReconciliationBusinessType(ReconciliationBusinessTypeEnum.PAYMENT);
                    tradeInfoList.add(tradeInfo);
                }
                ReconciliationStartMessage reconciliationStartMessage = new ReconciliationStartMessage();
                reconciliationStartMessage.setJobType(jobType);
                reconciliationStartMessage.setReconciliationTradeInfoList(tradeInfoList);
                rocketMqClient.sendMessage(PayMqTopicName.RECONCILIATION_START_TOPIC, JsonUtils.
                        toJsonString(reconciliationStartMessage));

                if (currentPage >= resultPage.getPages()) {
                    /**
                     * 更新jbs状态为已完成
                     */
                    reconciliationJobLogService.lambdaUpdate()
                            .eq(ReconciliationJobLogDO::getBatchId, bathId)
                            .eq(ReconciliationJobLogDO::getJobType, jobType)
                            .update();
                    log.info("批次号 {} 批次 {} 扫描任务已完成", bathId, jobType);
                    break;
                }
                
                currentPage++;
            }
            
            log.info("批次号 {} 交易单长款扫描完成", bathId);
        }

    }


}
