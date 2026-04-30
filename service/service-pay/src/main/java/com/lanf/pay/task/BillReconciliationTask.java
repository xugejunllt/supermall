package com.lanf.pay.task;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillExcelParseRetryMessage;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.pay.service.reconciliation.IFundBillDetailService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    /**
     * 下载账单到DB中
     * <p>
     * 使用mq 不同渠道不同mq任务处理
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void billSynchronizerTask() {

        log.info("开始执行T+1下载对账单任务");

        String relativeDateString = DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
        Set<PayChannelEnum> availableChannels = PayChannelEnum.AVAILABLE_CHANNELS;

        BillSynchronizerMessage billSynchronizerMessage = new BillSynchronizerMessage();

        billSynchronizerMessage.setBillType("signcustomer");

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
                /**
                * 发送一个超时检测任务 延迟1个小时
                */
                BillExcelParseRetryMessage billExcelParseRetryMessage = new BillExcelParseRetryMessage();
                billExcelParseRetryMessage.setBillType(billSynchronizerMessage.getBillType());
                billExcelParseRetryMessage.setBillDate(billSynchronizerMessage.getBillDate());
                billExcelParseRetryMessage.setPayChannel(billSynchronizerMessage.getPayChannel());
                billExcelParseRetryMessage.setFlowNo(flowNo);
                rocketMqClient.sendDelayMessage(PayMqTopicName.BILL_EXCEL_PARSE_RETRY_TOPIC,
                        JsonUtils.toJsonString(billExcelParseRetryMessage), TimeUnit.HOURS, 1);
            }

        }
        log.info("执行T+1定时下载对账单任务已启动");
    }


}
