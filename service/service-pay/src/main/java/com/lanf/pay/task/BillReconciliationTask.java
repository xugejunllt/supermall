package com.lanf.pay.task;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.DateUtils;
import com.lanf.pay.mapper.FundBillDetailMapper;
import com.lanf.pay.model.bo.BillSynchronizer;
import com.lanf.pay.model.entity.ChannelBillDownloadProgress;
import com.lanf.pay.model.entity.FundBillDetailDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 下载账单到DB中
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void billSynchronizerTask() {

        log.info("开始执行T+1下载对账单任务");

        String relativeDateString = DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
        List<PayChannelEnum> availableChannels = PayChannelEnum.AVAILABLE_CHANNELS;

        BillSynchronizer billSynchronizerMessage = new BillSynchronizer();

        billSynchronizerMessage.setBillType("signcustomer");

        billSynchronizerMessage.setBillDate(relativeDateString);
        for (PayChannelEnum channel : availableChannels) {

            billSynchronizerMessage.setPayChannel( channel);
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
                billSynchronizerMessage.setRetryCount(new AtomicInteger(0));
                billSynchronizerTask(billSynchronizerMessage);

            }
        }
        log.info("结束执行T+1定时下载对账单任务");
    }

    private static void billSynchronizerTask(BillSynchronizer billSynchronizer) {
        AtomicInteger retryCount = billSynchronizer.getRetryCount();
        int andIncrement = retryCount.get();
        if (andIncrement > 3) {
            log.error("重试次数超过3次，任务失败");
            return;
        }
        ThreadPoolTaskScheduler taskScheduler = BeanUtil.getBean(ThreadPoolTaskScheduler.class);
        taskScheduler.execute(new BillSynchronizerTask(billSynchronizer));
        //重试次数+1
        retryCount.getAndIncrement();
    }

    /**
     * 下载解析账单任务 并存储DB
     */
    static class BillSynchronizerTask implements Runnable {

        private final BillSynchronizer billSynchronizer;
        private final ThreadPoolTaskScheduler taskScheduler;
        private final FundBillDetailMapper fundBillDetailMapper;
        private final IChannelBillDownloadProgressService channelBillDownloadProgressService;

        public BillSynchronizerTask(BillSynchronizer billSynchronizer) {
            this.billSynchronizer = billSynchronizer;
            this.fundBillDetailMapper = BeanUtil.getBean(FundBillDetailMapper.class);
            this.channelBillDownloadProgressService = BeanUtil.getBean(IChannelBillDownloadProgressService.class);
            this.taskScheduler = (ThreadPoolTaskScheduler) BeanUtil.getBean("threadPoolTaskScheduler");
        }

        @Override
        public void run() {

            Future<?> future = taskScheduler.submit(() -> {
                /**
                 * 解析账单 存储账单
                 */
                List<FundBillDetailDO> batch;
                while (  !(batch = findBill()).isEmpty()){
                    try {
                        fundBillDetailMapper.batchInsertIgnore(batch);
                        batch.clear();
                    } catch (Exception e) {
                        log.warn("批量插入失败");

                    }
                }


            });
            try {
                future.get(2, TimeUnit.HOURS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                /**
                 * 超时 取消重试
                 */

                future.cancel(true);
                billSynchronizerTask( billSynchronizer);
            }
            /**
             * 账单解析成功
             */
            ChannelBillDownloadProgress one = channelBillDownloadProgressService.lambdaQuery()
                    .eq(ChannelBillDownloadProgress::getBatchId, billSynchronizer.getBillDate())
                    .eq(ChannelBillDownloadProgress::getPayChannel, billSynchronizer.getPayChannel())
                    .one();
            if (one == null) {
                log.error("该批次不存在");
                return;
            }
            if (BillDownloadStatusEnum.COMPLETED.equals(one.getStatus())) {
                log.warn("该批次已下载完成");
                return;
            }
            boolean update = channelBillDownloadProgressService.lambdaUpdate()
                    .eq(ChannelBillDownloadProgress::getBatchId, billSynchronizer.getBillDate())
                    .eq(ChannelBillDownloadProgress::getPayChannel, billSynchronizer.getPayChannel())
                    .eq(ChannelBillDownloadProgress::getVersion, one.getVersion())
                    .eq(ChannelBillDownloadProgress::getStatus, BillDownloadStatusEnum.DOWNLOADING)
                    .set(ChannelBillDownloadProgress::getVersion, one.getVersion() + 1)
                    .set(ChannelBillDownloadProgress::getStatus, BillDownloadStatusEnum.COMPLETED)
                    .update();
            if (!update) {
                log.warn("更新失败");
            }
        }
    }
    private static List<FundBillDetailDO> findBill() {

        return new ArrayList<>();
    }
}
