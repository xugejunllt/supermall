package com.lanf.pay.mq.listener;

import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.TransferSuccessMessage;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.TransferQueryResultBO;
import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.model.entity.TransferOrderFlowDO;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.model.enums.TransferStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.QueryTransferResultMessage;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import com.lanf.pay.service.pay.ITransferOrderService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.QUERY_TRANSFER_RESULT_TOPIC,
        consumerGroup = PayMqGroupName.QUERY_TRANSFER_RESULT_GROUP
)
public class QueryTransferResultListener implements RocketMQListener<QueryTransferResultMessage> {

    @Autowired
    private PaymentServiceFactory paymentServiceFactory;
    @Autowired
    private ITransferOrderFlowService transferOrderFlowService;

    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private ITransferOrderService transferOrderService;
    @MqRetryConsume(messageId = "#message.messageId")
    @Transactional
    @Override
    public void onMessage(QueryTransferResultMessage message) {

        log.info("监听到查询转账结果消息:{}", JsonUtils.toJsonString(message));

        String outBizNo = message.getOutBizNo();
        TransferOrderFlowDO one = transferOrderFlowService.lambdaQuery()
                .eq(TransferOrderFlowDO::getOutTradeNo, outBizNo)
                .one();
        if (one != null) {
            log.info("流水已存在");
            return;
        }

        TransferOrderDO oned = transferOrderService.lambdaQuery()
                .eq(TransferOrderDO::getOutTradeNo, outBizNo).one();

        if (oned == null) {
            log.error("转账单不存在");
            return;
        }

        PaymentService paymentService = paymentServiceFactory.getPaymentService(oned.getTransferChannel().getCode());
        TransferQueryResultBO queryResultBO = paymentService.queryTransferResult(message.getOutBizNo(), null);
        TransferOrderFlowDO transferOrderFlowDO = buildTransferOrderFlowDO(oned,message);
        TransferSuccessMessage transferSuccessMessage = buildTransferSuccessMessage(oned, queryResultBO);

        TransferStatusEnum transferStatusEnum = null;
        if (queryResultBO.getResult()) {
            transferStatusEnum = TransferStatusEnum.SUCCESS;
        } else {
            transferStatusEnum = TransferStatusEnum.FAILED;
        }

        try {
            transferOrderFlowService.save(transferOrderFlowDO);
        } catch (DuplicateKeyException e) {
            log.info("流水号已经存在");
            return;
        }
        boolean update = transferOrderService.lambdaUpdate()
                .eq(TransferOrderDO::getId, oned.getId())
                .eq(TransferOrderDO::getVersion, oned.getVersion())
                .eq(TransferOrderDO::getStatus, RefundStatusEnum.REFUNDING)
                .set(TransferOrderDO::getStatus, transferStatusEnum)
                .set(TransferOrderDO::getVersion, oned.getVersion() + 1)
                .update();

        if (!update) {
            log.warn("更新退款单失败");
            throw new MessageRetryConsumeException("更新退款单失败");
        }

        /**
         * 转账成功消息通知
         *
         */

        String tag = oned.getEventType().getTag();
        rocketMqClient.sendMessageWithTags(PayClientTopicName.TRANSFER_SUCCESS_EVENT_TOPIC, tag,
                JsonUtils.toJsonString(transferSuccessMessage));

    }

    private TransferSuccessMessage buildTransferSuccessMessage(TransferOrderDO oned, TransferQueryResultBO queryResultBO){
        TransferSuccessMessage transferSuccessMessage = new TransferSuccessMessage();
        transferSuccessMessage.setBizOrderId(oned.getBizOrderId());
        transferSuccessMessage.setEventType(oned.getEventType());
        transferSuccessMessage.setTransAmount(queryResultBO.getTransAmount());
        transferSuccessMessage.setResult( queryResultBO.getResult());
        return transferSuccessMessage;
    }

    private TransferOrderFlowDO buildTransferOrderFlowDO(TransferOrderDO oned,QueryTransferResultMessage message) {

        TransferOrderFlowDO transferOrderFlowDO = new TransferOrderFlowDO();
        transferOrderFlowDO.setOutTradeNo(oned.getOutTradeNo());
        transferOrderFlowDO.setTransferChannel(oned.getTransferChannel());
        transferOrderFlowDO.setFromAccount(oned.getFromAccount());
        transferOrderFlowDO.setIncomeAccount(oned.getIncomeAccount());
        transferOrderFlowDO.setTotalAmount(oned.getTotalAmount());
        transferOrderFlowDO.setPayFinishTime(message.getTransDate());
        transferOrderFlowDO.setPayFinishDate(DateUtils.format(message.getTransDate(),
                    DateUtils.DATE));

        return transferOrderFlowDO;
    }


}
