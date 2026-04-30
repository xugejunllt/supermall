package com.lanf.pay.mq.listener;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillExcelParseRetryMessage;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 解析账单 批量保存失败时
 * 通过mq进行补偿
 * mq可以自动进行重试
 *
 * 幂等优化：利用mq消费组件 基于 flowNo进行幂等
 * 或者新增一张表
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.BILL_EXCEL_PARSE_RETRY_TOPIC,
        consumerGroup = PayMqGroupName.BILL_EXCEL_PARSE_RETRY_GROUP
)
public class BillExcelParseRetryListener implements RocketMQListener<BillExcelParseRetryMessage> {


    @Autowired
    private IChannelBillDownloadProgressService channelBillDownloadProgressService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(BillExcelParseRetryMessage message) {

        String billDate = message.getBillDate();
        PayChannelEnum payChannel = message.getPayChannel();
        ChannelBillDownloadProgressDO one = channelBillDownloadProgressService.lambdaQuery()
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel)
                .eq(ChannelBillDownloadProgressDO::getBatchId, billDate).one();
        if (one == null) {
            log.error("该批次不存在");
            return;
        }
        if (BillDownloadStatusEnum.COMPLETED.equals(one.getStatus())) {
            log.info("解析任务已经完成");
            return;
        }

        BillSynchronizerMessage billSynchronizerMessage = new BillSynchronizerMessage();
        billSynchronizerMessage.setPayChannel(payChannel);
        billSynchronizerMessage.setBillDate(billDate);
        billSynchronizerMessage.setBillType(message.getBillType());
        billSynchronizerMessage.setFlowNo(one.getFlowNo());


        one.setFlowNo(null);
        one.setId(null);
        channelBillDownloadProgressService.removeById( one.getId());
        channelBillDownloadProgressService.save( one);

        rocketMqClient.sendMessage(PayMqTopicName.BILL_SYNCHRONIZER_TOPIC,
                JsonUtils.toJsonString(billSynchronizerMessage));



    }


}
