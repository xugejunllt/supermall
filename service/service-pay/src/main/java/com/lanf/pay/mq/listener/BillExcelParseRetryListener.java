package com.lanf.pay.mq.listener;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.IdUtils;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析账单 批量保存失败时
 * 通过mq进行补偿
 * mq可以自动进行重试
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
    private RocketMqClient rocketMqClient ;

    @Transactional
    @Override
    public void onMessage(BillExcelParseRetryMessage message) {

        String billDate = message.getBillDate();

        List<ChannelBillDownloadProgressDO> downloadProgressDOS = channelBillDownloadProgressService.lambdaQuery()
                .eq(ChannelBillDownloadProgressDO::getBatchId, billDate)
                .list();
        List<PayChannelEnum>  notFinishedPayChannel = new ArrayList<>();
        boolean allFinished = true ;
        for (ChannelBillDownloadProgressDO downloadProgressDO : downloadProgressDOS) {

            if ( !BillDownloadStatusEnum.COMPLETED.equals(downloadProgressDO.getStatus())) {
                allFinished = false;
                notFinishedPayChannel.add(downloadProgressDO.getPayChannel());
            }
        }
         if (allFinished){
             log.info("所有渠道账单下载完成");
             /**
              * 开始进行对账任务
              */

         } else {
             /**
              * 删除旧的账单
              * 重新插入新的账单
              */
             for (PayChannelEnum payChannel : notFinishedPayChannel){

                 ChannelBillDownloadProgressDO save = new ChannelBillDownloadProgressDO();
                 save.setBatchId(billDate);
                 save.setPayChannel(payChannel);
                 save.setStatus(BillDownloadStatusEnum.DOWNLOADING);

                 ChannelBillDownloadProgressDO one = channelBillDownloadProgressService.lambdaQuery()
                         .eq(ChannelBillDownloadProgressDO::getBatchId, billDate)
                         .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel)
                         .one();
                 BillSynchronizerMessage billSynchronizerMessage = new BillSynchronizerMessage();
                 billSynchronizerMessage.setPayChannel(payChannel);
                 billSynchronizerMessage.setBillType(message.getBillType());
                 billSynchronizerMessage.setBillDate(message.getBillDate());
                 billSynchronizerMessage.setFlowNo(IdUtils.generateId() + "");

                 channelBillDownloadProgressService.removeById( one.getId());
                 try {
                     channelBillDownloadProgressService.save(save);
                 } catch (DuplicateKeyException e) {
                     log.info("渠道账单下载进度已存在");
                     return;
                 }
                //2.发起下载解析任务
                 rocketMqClient.sendMessage(PayMqTopicName.BILL_SYNCHRONIZER_TOPIC, JsonUtils.toJsonString(billSynchronizerMessage));

             }

         }

    }


}
