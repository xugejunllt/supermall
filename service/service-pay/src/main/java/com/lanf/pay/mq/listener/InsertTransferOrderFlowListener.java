package com.lanf.pay.mq.listener;

/**
 * 插入流水
 */

import com.lanf.common.utils.DateUtils;
import com.lanf.pay.model.entity.TransferOrderFlowDO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.TransferQueryResultProcessorMessage;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.TRANSFER_QUERY_RESULT_TOPIC,
        consumerGroup = PayMqGroupName.INSERT_TRANSFER_ORDER_FLOW_GROUP
)
public class InsertTransferOrderFlowListener implements RocketMQListener<TransferQueryResultProcessorMessage> {

    @Autowired
    private ITransferOrderFlowService transferOrderFlowService;


    @Override
    public void onMessage(TransferQueryResultProcessorMessage message) {


        TransferOrderFlowDO one = transferOrderFlowService.lambdaQuery()
                .eq(TransferOrderFlowDO::getOutTradeNo, message.getOutTradeNo()).one();
        if ( one!= null) {
            log.info("流水号已经存在");
            return;
        }
        TransferOrderFlowDO transferOrderFlowDO = new TransferOrderFlowDO();
        transferOrderFlowDO.setOutTradeNo(message.getOutTradeNo());
        transferOrderFlowDO.setTransferChannel(message.getTransferChannel());
        transferOrderFlowDO.setFromAccount(message.getFromAccount());
        transferOrderFlowDO.setIncomeAccount(message.getIncomeAccount());
        transferOrderFlowDO.setTotalAmount(message.getTotalAmount());
        transferOrderFlowDO.setTransAmount(message.getTransAmount());
        transferOrderFlowDO.setStatus(message.getStatus());
        transferOrderFlowDO.setPayFinishTime(message.getPayFinishTime());
        transferOrderFlowDO.setPayFinishDate(DateUtils.format(message.getPayFinishTime(),
                DateUtils.DATE));
        transferOrderFlowDO.setFailReason(message.getFailReason());

        try {
            transferOrderFlowService.save(transferOrderFlowDO);
        } catch (DuplicateKeyException e) {
            log.info("流水号已经存在");
        }
    }
}
