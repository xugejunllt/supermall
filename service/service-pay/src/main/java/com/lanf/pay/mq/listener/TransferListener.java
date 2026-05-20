package com.lanf.pay.mq.listener;

import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.TransferMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.TransferResult;
import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.model.enums.TransferStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.message.QueryTransferResultMessage;
import com.lanf.pay.service.pay.ITransferOrderService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import static com.lanf.pay.mq.constant.PayMqTopicName.QUERY_TRANSFER_RESULT_TOPIC;

/**
 * 处理转账事件
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.TRANSFER_TOPIC,
        consumerGroup = PayMqGroupName.TRANSFER_GROUP
)
public class TransferListener implements RocketMQListener<TransferMessage> {
    @Autowired
    private PaymentServiceFactory paymentServiceFactory;
    @Autowired
    private ITransferOrderService transferOrderService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(TransferMessage message) {

        log.info("监听转账事件:{}", message);

        String outBizNo = message.getOutBizNo();
        TransferOrderDO one = transferOrderService.lambdaQuery().eq(TransferOrderDO::getOutTradeNo,
                outBizNo).one();
        if (one == null) {
            TransferOrderDO transferOrderDO = buildTransferOrderDO(message);
            try {
                transferOrderService.save(transferOrderDO);
            } catch (DuplicateKeyException e) {
                log.warn("该转账单已存在");

            }

        }
        /**
         *
         * 发起转账
         */
        PaymentService paymentService = paymentServiceFactory.getPaymentService(message.getTransferChannel().getCode());
        TransferResult result = paymentService.transfer(message.getOutBizNo(),
                message.getIncomeAccount(), message.getTransAmount(), message.getOrderTitle());

        QueryTransferResultMessage queryTransferResultMessage = getQueryTransferResultMessage(message);
        rocketMqClient.sendMessage(QUERY_TRANSFER_RESULT_TOPIC,JsonUtils.toJsonString(queryTransferResultMessage));
    }

    private static QueryTransferResultMessage getQueryTransferResultMessage(TransferMessage message) {
        QueryTransferResultMessage queryTransferResultMessage = new QueryTransferResultMessage();
        queryTransferResultMessage.setOutBizNo(message.getOutBizNo());
        return queryTransferResultMessage;
    }


    private static TransferOrderDO buildTransferOrderDO(TransferMessage message) {

        TransferOrderDO transferOrderDO = new TransferOrderDO();
        transferOrderDO.setOutTradeNo(message.getOutBizNo());
        transferOrderDO.setUserId(message.getUserId());
        transferOrderDO.setMerchantId(message.getMerchantId());
        transferOrderDO.setBizOrderId(message.getBizOrderId());
        transferOrderDO.setEventType(message.getEventType());
        transferOrderDO.setTransferChannel(message.getTransferChannel());
        transferOrderDO.setFromAccount(message.getFromAccount());
        transferOrderDO.setIncomeAccount(message.getIncomeAccount());
        transferOrderDO.setTotalAmount(message.getTransAmount());
        transferOrderDO.setStatus(TransferStatusEnum.REFUNDING);

        return transferOrderDO;
    }



}
