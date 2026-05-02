package com.lanf.pay.mq.listener;

/**
 * 查询退款结果
 */

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.client.pay.model.enums.RefundEventTypeEnum;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.enums.FlowNoPrefixEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.constant.FinanceClientTopicName;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.pay.model.bo.RefundQueryResultBO;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.model.entity.RefundOrderFlowDO;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.QueryRefundResultMessage;
import com.lanf.pay.service.pay.IRefundOrderFlowService;
import com.lanf.pay.service.pay.IRefundOrderService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private IRefundOrderFlowService refundOrderFlowService;
    @Autowired
    private IRefundOrderService refundOrderService;

    @Transactional
    @Override
    public void onMessage(QueryRefundResultMessage message) {


        RefundOrderFlowDO one = refundOrderFlowService.lambdaQuery().
                eq(RefundOrderFlowDO::getOutTradeNo, message.getOutTradeNo())
                .eq(RefundOrderFlowDO::getOutRequestNo, message.getOutRequestNo()).one();

        if (one != null) {
            log.info("退款已处理");
            return;
        }
        PayChannelEnum payChannel = message.getPayChannel();

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payChannel.getCode());

        RefundQueryResultBO refundQueryResultBO = paymentService.
                queryRefundResult(message.getOutTradeNo(), message.getOutRequestNo());


        RefundOrderDO orderDO = refundOrderService.lambdaQuery().eq(RefundOrderDO::getOutTradeNo, message.getOutTradeNo()).one();
        if (orderDO == null) {
            log.error("退款单不存在");
            throw new BizException("退款单不存在");
        }
        AddMoneyFlowMessage addMoneyFlowMessage = buildAddMoneyFlowMessage(refundQueryResultBO.getSendBackFee(), orderDO);
        RefundOrderFlowDO refundOrderFlowDO = buildRefundOrderFlowDO(message,
                refundQueryResultBO);

        RefundStatusEnum refundStatusEnum = null;
        if (refundQueryResultBO.getResult()) {
            refundStatusEnum = RefundStatusEnum.SUCCESS;
        } else {
            refundStatusEnum = RefundStatusEnum.FAILED;
        }
        try {
            refundOrderFlowService.save(refundOrderFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("退款流水已存在");
            throw new BizException("退款流水已存在");
        }
        boolean update = refundOrderService.lambdaUpdate()
                .eq(RefundOrderDO::getId, orderDO.getId())
                .eq(RefundOrderDO::getVersion, orderDO.getVersion())
                .eq(RefundOrderDO::getStatus, RefundStatusEnum.REFUNDING)
                .set(RefundOrderDO::getStatus, refundStatusEnum)
                .set(RefundOrderDO::getVersion, orderDO.getVersion() + 1)
                .update();

        if (!update) {
            log.warn("更新退款单失败");
            throw new MessageRetryConsumeException("更新退款单失败");
        }
        if (refundQueryResultBO.getResult()) {
            /**
             * 插入资金流水
             */
            rocketMqClient.sendMessage(FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC, JsonUtils.toJsonString(addMoneyFlowMessage));
        }
    }

    private RefundOrderFlowDO buildRefundOrderFlowDO(QueryRefundResultMessage message,
                                                     RefundQueryResultBO refundQueryResultBO) {
        RefundFlowStatusEnum refundFlowStatusEnum = null;
        if (refundQueryResultBO.getResult()) {
            refundFlowStatusEnum = RefundFlowStatusEnum.SUCCESS;
        } else {
            refundFlowStatusEnum = RefundFlowStatusEnum.FAILED;
        }
        RefundOrderFlowDO refundOrderFlowDO = new RefundOrderFlowDO();
        refundOrderFlowDO.setOutTradeNo(message.getOutTradeNo());
        refundOrderFlowDO.setOutRequestNo(message.getOutRequestNo());
        refundOrderFlowDO.setTradeNo(refundQueryResultBO.getTradeNo());
        refundOrderFlowDO.setPayMoney(refundQueryResultBO.getRefundAmount());
        refundOrderFlowDO.setReturnMoney(refundQueryResultBO.getSendBackFee());
        refundOrderFlowDO.setStatus(refundFlowStatusEnum);
        refundOrderFlowDO.setPayChannelEnum(message.getPayChannel());
        refundOrderFlowDO.setPayFinishTime(refundQueryResultBO.getGmtRefundPay());
        refundOrderFlowDO.setPayFinishDate(DateUtils.format(refundQueryResultBO.getGmtRefundPay(),
                DateUtils.DATE));
        refundOrderFlowDO.setFailReason(refundQueryResultBO.getErrorMsg());

        return refundOrderFlowDO;
    }


    private AddMoneyFlowMessage buildAddMoneyFlowMessage(BigDecimal incomeMoney, RefundOrderDO orderDO) {


        RefundEventTypeEnum refundEventType = orderDO.getRefundEventType();
        RecordTypeEnum recordTypeEnum = null;

        if (refundEventType

                .equals(RefundEventTypeEnum.CANCEL_PAID_ORDER)) {
            recordTypeEnum = RecordTypeEnum.CANCEL_ORDER_REFUND;

        } else if (refundEventType
                .equals(RefundEventTypeEnum.AFTER_SALES_REFUND)) {
            recordTypeEnum = RecordTypeEnum.AFTER_SALES_REFUND;
        } else {
            log.error("退款事件类型异常");
            throw new BizException("退款事件类型异常");
        }
        AddMoneyFlowMessage moneyFlowMessage = new AddMoneyFlowMessage();
        moneyFlowMessage.setIncomeMoney(incomeMoney);
        moneyFlowMessage.setRecordType(recordTypeEnum);
        moneyFlowMessage.setFlowNo(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.MONEY_FLOW,orderDO.getId().toString()));
        moneyFlowMessage.setBusinessId(null);
        moneyFlowMessage.setBizOrderId(orderDO.getBizOrderId());

        return moneyFlowMessage;
    }


}
