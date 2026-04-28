package com.lanf.pay.mq.listener;

import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.message.TransferMessage;
import com.lanf.pay.model.bo.TransferResult;
import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.pay.ITransferOrderService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 转账
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.TRANSFER_TOPIC,
        consumerGroup = PayMqGroupName.TRANSFER_GROUP
)
public class TransferListener implements RocketMQListener<TransferMessage> {
    
    @Autowired
    private ITransferOrderService transferOrderService;

    @Override
    public void onMessage(TransferMessage message) {

        log.info("监听转账事件:{}", message);

        String outBizNo = message.getOutBizNo();
        TransferOrderDO one = transferOrderService.lambdaQuery().eq(TransferOrderDO::getOutBizNo, outBizNo).one();
        if (one != null) {
            log.warn("该转账单已存在");
            return;
        }
        
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(message.getTransferChannel().getCode());
        TransferResult result = paymentService.alipayTransfer(message.getOutBizNo(),
                message.getIncomeAccount(), message.getTransAmount(), message.getOrderTitle());
        Boolean transferSuccess = result.getTransferSuccess();
        if (transferSuccess) {
            log.info("转账成功:{}", result);
            TransferOrderDO transferOrderDO = buildTransferOrderDO(message, result);

            try {
                transferOrderService.save(transferOrderDO);
            } catch (DuplicateKeyException e) {
               log.warn("该转账单已存在");
               return;
            }

        } else {
            log.warn("转账失败");
           
        }


    }


    private static TransferOrderDO buildTransferOrderDO(TransferMessage message, TransferResult result) {
        TransferOrderDO transferOrderDO = new TransferOrderDO();
        transferOrderDO.setOutBizNo(message.getOutBizNo());
        transferOrderDO.setUserId(message.getUserId());
        transferOrderDO.setMerchantId(message.getMerchantId());
        transferOrderDO.setBizOrderId(message.getBizOrderId());
        transferOrderDO.setEventType(message.getEventType());
        transferOrderDO.setTransferChannel(message.getTransferChannel());
        transferOrderDO.setFromAccount(message.getFromAccount());
        transferOrderDO.setIncomeAccount(message.getIncomeAccount());
        transferOrderDO.setTransAmount(message.getTransAmount());
        transferOrderDO.setOrderId(result.getOrderId());
        transferOrderDO.setPayFinishTime(result.getFinishTime());
        transferOrderDO.setOrderTitle(message.getOrderTitle());
        return transferOrderDO;
    }

}
