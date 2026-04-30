package com.lanf.pay.mq.listener;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillExcelParseRetryMessage;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 解析账单 批量保存失败时
 * 通过mq进行补偿
 * mq可以自动进行重试
 *
 * 存在重复消费情况
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
        one.setFlowNo(null);
        one.setId(null);
        channelBillDownloadProgressService.removeById( one.getId());
        channelBillDownloadProgressService.save( one);

        /**
         * 发送一个超时检测任务 延迟1个小时
         */
        BillExcelParseRetryMessage billExcelParseRetryMessage = new BillExcelParseRetryMessage();
        billExcelParseRetryMessage.setBillType(message.getBillType());
        billExcelParseRetryMessage.setBillDate(message.getBillDate());
        billExcelParseRetryMessage.setPayChannel(message.getPayChannel());
        rocketMqClient.sendDelayMessage(PayMqTopicName.BILL_EXCEL_PARSE_RETRY_TOPIC,
                JsonUtils.toJsonString(billExcelParseRetryMessage), TimeUnit.HOURS, 1);



    }


}
