package com.lanf.pay.mq.listener;

import com.lanf.client.pay.model.enums.TransferEventTypeEnum;
import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.message.TransferMessage;
import com.lanf.client.pay.mq.message.TransferSuccessMessage;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.constant.FinanceClientTopicName;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.pay.model.bo.TransferQueryResultBO;
import com.lanf.pay.model.bo.TransferResult;
import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.model.entity.TransferOrderFlowDO;
import com.lanf.pay.model.enums.TransferFlowStatusEnum;
import com.lanf.pay.model.enums.TransferStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.QueryTransferResultMessage;
import com.lanf.pay.mq.message.TransferQueryResultProcessorMessage;
import com.lanf.pay.service.pay.IRefundOrderService;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 *
 *
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
    private ITransferOrderFlowService transferOrderFlowService;]

    @Autowired
    private RocketMqClient rocketMqClient;


    @Override
    public void onMessage(QueryTransferResultMessage message) {


        String outBizNo = message.getOutBizNo();
        TransferOrderFlowDO one = transferOrderFlowService.lambdaQuery().eq(TransferOrderFlowDO::getOutTradeNo, outBizNo)
                .one();
        if (one != null) {
            log.info("流水已存在");
            return;
        }

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(message.getTransferChannel().getCode());
        TransferQueryResultBO queryResultBO = paymentService.queryTransferResult(message.getOutBizNo(), null);
        TransferQueryResultProcessorMessage resultProcessorMessage = buildTransferQueryResultProcessorMessage(queryResultBO, message);
        rocketMqClient.sendMessage(PayMqTopicName.TRANSFER_QUERY_RESULT_TOPIC, resultProcessorMessage);




        AddMoneyFlowMessage addMoneyFlowMessage = buildAddMoneyFlowMessage(message, queryResultBO.getTransAmount());
        if (queryResultBO.getResult()){


            /**
             * 发送消息添加到资金流水
             */
            rocketMqClient.sendMessage(FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC, JsonUtils.toJsonString(addMoneyFlowMessage));
            log.info("发送转账成功消息完成，eventType:{}, outBizNo:{}", message.getEventType(), outBizNo);

        }
        z
        /**
         * 转账成功消息通知
         *
         */
        String tag = message.getEventType().getTag();
        rocketMqClient.sendMessageWithTags(PayClientTopicName.TRANSFER_SUCCESS_EVENT_TOPIC, tag,
                JsonUtils.toJsonString(addMoneyFlowMessage));

    }

    private TransferQueryResultProcessorMessage buildTransferQueryResultProcessorMessage(
            TransferQueryResultBO queryResultBO,QueryTransferResultMessage message){

        TransferFlowStatusEnum flowStatusEnum = null;
        TransferStatusEnum updateTransferStatus = null;
        if ( queryResultBO.getResult()){
            flowStatusEnum = TransferFlowStatusEnum.SUCCESS;
            updateTransferStatus = TransferStatusEnum.SUCCESS;
        } else {
            flowStatusEnum = TransferFlowStatusEnum.FAILED;
            updateTransferStatus = TransferStatusEnum.FAILED;
        }

        TransferQueryResultProcessorMessage resultProcessorMessage = new TransferQueryResultProcessorMessage();
        resultProcessorMessage.setOutTradeNo(resultProcessorMessage.getOutTradeNo());
        resultProcessorMessage.setTransferChannel(message.getTransferChannel());
        resultProcessorMessage.setFromAccount(message.getFromAccount());
        resultProcessorMessage.setIncomeAccount(message.getIncomeAccount());
        resultProcessorMessage.setTotalAmount(message.getTransAmount());
        resultProcessorMessage.setTransAmount(queryResultBO.getTransAmount());
        resultProcessorMessage.setStatus(flowStatusEnum);
        resultProcessorMessage.setPayFinishTime(queryResultBO.getFinishTime());
        resultProcessorMessage.setFailReason(queryResultBO.getErrorMsg());
        resultProcessorMessage.setUpdateTransferStatus(updateTransferStatus);
        return resultProcessorMessage;
    }


    private AddMoneyFlowMessage buildAddMoneyFlowMessage(QueryTransferResultMessage message, BigDecimal incomeMoney) {
        RecordTypeEnum recordType = null;
        TransferEventTypeEnum eventType = message.getEventType();
        switch (eventType) {
            case ORDER_SETTLEMENT:
                recordType = RecordTypeEnum.MERCHANT_SETTLEMENT_INCOME;
                break;
            case WALLET_WITHDRAW:
                recordType = RecordTypeEnum.WALLET_WITHDRAW;
                break;
            default:
                log.error("不支持的转账事件");
                throw new BizException("不支持的转账事件");
        }
        AddMoneyFlowMessage addMoneyFlowMessage = new AddMoneyFlowMessage();
        addMoneyFlowMessage.setBusinessId(message.getMerchantId());
        addMoneyFlowMessage.setBizOrderId(message.getBizOrderId());
        addMoneyFlowMessage.setFlowNo(CodeGenerateUtils.generateSerialNumber(message.getBizOrderId().toString()));
        addMoneyFlowMessage.setRecordType(recordType);
        addMoneyFlowMessage.setIncomeMoney(incomeMoney);
        return addMoneyFlowMessage;
    }




    private static TransferSuccessMessage buildTransferSuccessMessage(TransferMessage message) {
        TransferSuccessMessage successMessage = new TransferSuccessMessage();

        successMessage.setBizOrderId(message.getBizOrderId());
        successMessage.setEventType(message.getEventType());
        successMessage.setTransAmount(message.getTransAmount());

        return successMessage;
    }

}
