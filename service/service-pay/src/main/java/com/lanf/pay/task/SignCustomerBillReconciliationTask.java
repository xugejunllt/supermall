package com.lanf.pay.task;

import com.lanf.api.pay.model.enums.*;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.pay.service.reconciliation.IReconciliationResultService;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * T+1对账任务
 * 多机器并发执行task
 */
@Slf4j
@Component
public class SignCustomerBillReconciliationTask {

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Autowired
    private IChannelBillDownloadProgressService channelBillDownloadProgressService;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private SignCustomerIFundBillDetailService fundBillDetailService;

    @Autowired
    private IReconciliationResultService reconciliationResultService;

    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;

    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    private final String billType = BillTypeEnum.SIGN_CUSTOMER.getCode();

    /**
     * 下载 Trade 账单到DB中
     * <p>
     * 使用mq 不同渠道不同mq任务处理
     * <p>
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Shanghai")
    public void billTradeSynchronizerTask() {

        log.info("开始执行T+1下载对账单任务");
        String bathId = BatchIdContext.getBatchId();
        if (bathId == null) {
            bathId = getBathId();
        }
        try {
            BatchIdContext.setBatchId(bathId);
            List<PayChannelEnum> availableChannels = PayChannelEnum.AVAILABLE_CHANNELS;

            BillSynchronizerMessage billSynchronizerMessage = new BillSynchronizerMessage();

            billSynchronizerMessage.setBillType(billType);

            billSynchronizerMessage.setBillDate(bathId);
            for (PayChannelEnum channel : availableChannels) {


                boolean downloadProgress = channelBillDownloadProgressService.addChannelBillDownloadProgress(bathId,
                        channel);
                if (!downloadProgress) {
                    log.info("{}账单正在下载中", channel);
                    continue;
                }

                billSynchronizerMessage.setPayChannel(channel);
                mqSendMessageUtils.sendMessage(PayMqTopicName.BILL_SYNCHRONIZER_TOPIC,
                        JsonUtils.toJsonString(billSynchronizerMessage),null);


            }
            log.info("执行T+1定时下载对账单任务已启动");
        } finally {
            BatchIdContext.clear();
        }
    }

    /**
     * 每天10点之后 每隔1小时执行一次
     * 检查当前  账单是否全部解析完成
     * 如果没有 进行重新投递
     */
    @Scheduled(cron = "0 0 10-23 * * ?", zone = "Asia/Shanghai")
    public void isTradeBillParsedTask() {

        String bathId = getBathId();
        try {
            BatchIdContext.setBatchId(bathId);

            List<ChannelBillDownloadProgressDO> downloadProgressDOS =
                    channelBillDownloadProgressService.lambdaQuery()
                            .eq(ChannelBillDownloadProgressDO::getBillType, billType)
                            .eq(ChannelBillDownloadProgressDO::getBatchId, bathId).list();

            for (ChannelBillDownloadProgressDO downloadProgressDO : downloadProgressDOS) {

                BillDownloadStatusEnum status = downloadProgressDO.getStatus();

                if (BillDownloadStatusEnum.DOWNLOADING.equals(status)) {
                    log.info("{}账单正在下载中,发送补投任务", downloadProgressDO.getPayChannel());

                    try {

                        channelBillDownloadProgressService.redeliverTask(downloadProgressDO, billType);

                    } catch (Exception e) {
                        log.error("发送补投任务异常", e);
                    }

                }

            }


        } finally {
            BatchIdContext.clear();
        }
    }

    /**
     * 每天10点之后 每30分钟执行一次
     * 检查当前 Trade账单 是否全部解析完成
     */
    @Scheduled(cron = "0 0/30 10-23 * * ?", zone = "Asia/Shanghai")
    public void isTradeAllBillParsedTask() {

        String bathId = BatchIdContext.getBatchId();
        if (bathId == null) {
            bathId = getBathId();
        }
        try {
            BatchIdContext.setBatchId(bathId);
            List<ChannelBillDownloadProgressDO> downloadProgressDOS =
                    channelBillDownloadProgressService.lambdaQuery()
                            .eq(ChannelBillDownloadProgressDO::getBillType, billType)
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
            for (ReconciliationJobTypeEnum jobType : ReconciliationJobTypeEnum.TRADE_AND_REFUND_SET) {
                BillScanTask billScanTask = new BillScanTask(bathId, jobType);

                ReconciliationJobLogDO one = reconciliationJobLogService.lambdaQuery()
                        .eq(ReconciliationJobLogDO::getBatchId, bathId)
                        .eq(ReconciliationJobLogDO::getJobType, jobType).one();

                if (one != null && one.getStatus().equals(ReconciliationJobStatusEnum.SCAN_COMPLETED)) {
                    log.warn("该对账任务已完成");
                    continue;
                }

                taskScheduler.execute(billScanTask);
            }
            log.info("批次号 {} 的对账任务已提交", bathId);
        } finally {
            BatchIdContext.clear();
        }
    }

    private String getBathId() {
        return DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
    }

    /**
     * 检查扫描任务是否完成
     */
    @Scheduled(cron = "0 0 12-23 * * ?", zone = "Asia/Shanghai")
    public void billScanCompletionCheckerTask() {

        String bathId = getBathId();
        try {
            BatchIdContext.setBatchId(bathId);
            List<ReconciliationJobLogDO> jobLogDOList = reconciliationJobLogService.lambdaQuery()
                    .eq(ReconciliationJobLogDO::getBatchId, bathId)
                    .list();
            for (ReconciliationJobLogDO jobLogDO : jobLogDOList) {

                if (jobLogDO.getStatus().isExecuting()) {
                    log.info("批次号 {} 的任务 {} 正在执行,重新投递", bathId, jobLogDO.getJobType());
                    BillScanTask billScanTask = new BillScanTask(bathId, jobLogDO.getJobType());
                    taskScheduler.execute(billScanTask);
                }

            }


        } finally {
            BatchIdContext.clear();
        }
    }


}
