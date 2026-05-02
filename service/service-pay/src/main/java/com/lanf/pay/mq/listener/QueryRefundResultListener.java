package com.lanf.pay.mq.listener;

/**
 * 查询退款结果
 */

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.client.pay.model.enums.RefundEventTypeEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.exception.BizException;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.constant.FinanceClientTopicName;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.pay.model.bo.RefundQueryResultBO;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.QueryRefundResultMessage;
import com.lanf.pay.mq.message.RefundQueryResultProcessorMessage;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 查询退款结果
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.QUERY_REFUND_RESULT_TOPIC,
        consumerGroup = PayMqGroupName.QUERY_REFUND_RESULT_GROUP
)
public class QueryRefundResultListener implements RocketMQListener<QueryRefundResultMessage> {

    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(QueryRefundResultMessage message) {

        PayChannelEnum payChannel = message.getPayChannel();
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payChannel.getCode());
        RefundQueryResultBO refundQueryResultBO = paymentService.
                queryRefundResult(message.getOutTradeNo(), message.getOutRequestNo());
        RefundFlowStatusEnum refundFlowStatusEnum = null;
        RefundStatusEnum refundStatusEnum = null;
        if (refundQueryResultBO.getResult()) {
            refundFlowStatusEnum = RefundFlowStatusEnum.SUCCESS;
            refundStatusEnum = RefundStatusEnum.SUCCESS;
        } else {
            refundFlowStatusEnum = RefundFlowStatusEnum.FAILED;
            refundStatusEnum = RefundStatusEnum.FAILED;
        }

        RefundQueryResultProcessorMessage queryRefundResultProcessorMessage =
                new RefundQueryResultProcessorMessage();
        queryRefundResultProcessorMessage.setOutTradeNo(refundQueryResultBO.getOutTradeNo());
        queryRefundResultProcessorMessage.setOutRequestNo(refundQueryResultBO.getOutRequestNo());
        queryRefundResultProcessorMessage.setTradeNo(refundQueryResultBO.getTradeNo());
        queryRefundResultProcessorMessage.setPayMoney(refundQueryResultBO.getRefundAmount());
        queryRefundResultProcessorMessage.setReturnMoney(refundQueryResultBO.getSendBackFee());
        queryRefundResultProcessorMessage.setStatus(refundFlowStatusEnum);
        queryRefundResultProcessorMessage.setPayOrderId(message.getPayOrderId());
        queryRefundResultProcessorMessage.setPayChannelEnum(payChannel);
        queryRefundResultProcessorMessage.setPayFinishTime(refundQueryResultBO.getGmtRefundPay());
        queryRefundResultProcessorMessage.setUpdateStatusRefundStatus(RefundStatusEnum.SUCCESS);
        queryRefundResultProcessorMessage.setFailReason(refundQueryResultBO.getErrorMsg());
        rocketMqClient.sendMessage(PayMqTopicName.QUERY_REFUND_RESULT_TOPIC, JsonUtils.toJsonString(queryRefundResultProcessorMessage));
        if (refundQueryResultBO.getResult()){
            /**
             * 插入资金流水
             */
            AddMoneyFlowMessage addMoneyFlowMessage = buildAddMoneyFlowMessage(message, refundQueryResultBO.getSendBackFee());
            rocketMqClient.sendMessage(FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC, JsonUtils.toJsonString(addMoneyFlowMessage));

        }
    }


    private AddMoneyFlowMessage buildAddMoneyFlowMessage( QueryRefundResultMessage message,BigDecimal incomeMoney){
        RecordTypeEnum recordTypeEnum = null;

        if (message.getRefundEventTypeEnum()

                .equals(RefundEventTypeEnum.CANCEL_PAID_ORDER)){
            recordTypeEnum = RecordTypeEnum.CANCEL_ORDER_REFUND;

        } else if (message.getRefundEventTypeEnum()
                .equals(RefundEventTypeEnum.AFTER_SALES_REFUND)){
            recordTypeEnum = RecordTypeEnum.AFTER_SALES_REFUND;
        } else {
            log.error("退款事件类型异常");
            throw new BizException("退款事件类型异常");
        }

        AddMoneyFlowMessage moneyFlowMessage = new AddMoneyFlowMessage();
        moneyFlowMessage.setBusinessId(Constants.PLATFORM_BUSINESS_ID);
        moneyFlowMessage.setBizOrderId(message.getBizOrderId());
        moneyFlowMessage.setIncomeMoney(incomeMoney);
        moneyFlowMessage.setRecordType(recordTypeEnum);

        return moneyFlowMessage;
    }


}
