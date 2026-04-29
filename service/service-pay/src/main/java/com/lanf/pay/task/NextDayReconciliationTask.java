package com.lanf.pay.task;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * T+1对账任务
 *
 */
@Slf4j
@Component
public class NextDayReconciliationTask {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Scheduled(cron = "0/5 * * * * *")
    public void nextDayReconciliationTask() {

        log.info("开始执行T+1下载对账单任务");

        String relativeDateString = DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
        List<PayChannelEnum> availableChannels = PayChannelEnum.AVAILABLE_CHANNELS;
        BillSynchronizerMessage billSynchronizerMessage = new BillSynchronizerMessage();
        billSynchronizerMessage.setBillType("signcustomer");
        billSynchronizerMessage.setBillDate(relativeDateString);
        for (PayChannelEnum channel : availableChannels) {
            billSynchronizerMessage.setPayChannel( channel);
            rocketMqClient.sendMessage(PayMqTopicName.BILL_SYNCHRONIZER_TOPIC, JsonUtils.toJsonString(billSynchronizerMessage));

        }

    }


}
