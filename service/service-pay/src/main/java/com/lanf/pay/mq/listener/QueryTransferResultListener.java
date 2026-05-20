package com.lanf.pay.mq.listener;

import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.TransferSuccessMessage;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.constant.FinanceClientTopicName;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.pay.model.bo.TransferQueryResultBO;
import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.model.entity.TransferOrderFlowDO;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.model.enums.TransferFlowStatusEnum;
import com.lanf.pay.model.enums.TransferStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.QueryTransferResultMessage;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import com.lanf.pay.service.pay.ITransferOrderService;
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

    @Transactional
    @Override
    public void onMessage(QueryTransferResultMessage message) {


        String outBizNo = message.getOutBizNo();
        TransferOrderFlowDO one = transferOrderFlowService.lambdaQuery().eq(TransferOrderFlowDO::getOutTradeNo, outBizNo)
                .one();
        if (one != null) {
            log.info("流水已存在");
            return;
        }

        TransferOrderDO oned = transferOrderService.lambdaQuery().eq(TransferOrderDO::getOutTradeNo, outBizNo).one();

        if (oned == null) {
            log.error("转账单不存在");
            return;
        }

        PaymentService paymentService = paymentServiceFactory.getPaymentService(oned.getTransferChannel().getCode());
        TransferQueryResultBO queryResultBO = paymentService.queryTransferResult(message.getOutBizNo(), null);
        AddMoneyFlowMessage addMoneyFlowMessage = buildAddMoneyFlowMessage(oned, queryResultBO.getTransAmount());


        TransferOrderFlowDO transferOrderFlowDO = buildTransferOrderFlowDO(oned, queryResultBO);

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
        if (queryResultBO.getResult()) {

            /**
             * 发送消息添加到资金流水
             */
            rocketMqClient.sendMessage(FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC, JsonUtils.toJsonString(addMoneyFlowMessage));

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

    private TransferOrderFlowDO buildTransferOrderFlowDO(TransferOrderDO oned, TransferQueryResultBO queryResultBO) {
        TransferFlowStatusEnum transferFlowStatus = null;
        if (queryResultBO.getResult()) {
            transferFlowStatus = TransferFlowStatusEnum.SUCCESS;
        } else {
            transferFlowStatus = TransferFlowStatusEnum.FAILED;
        }
        TransferOrderFlowDO transferOrderFlowDO = new TransferOrderFlowDO();
        transferOrderFlowDO.setOutTradeNo(oned.getOutTradeNo());
        transferOrderFlowDO.setTransferChannel(oned.getTransferChannel());
        transferOrderFlowDO.setFromAccount(oned.getFromAccount());
        transferOrderFlowDO.setIncomeAccount(oned.getIncomeAccount());
        transferOrderFlowDO.setTotalAmount(oned.getTotalAmount());
        transferOrderFlowDO.setTransAmount(queryResultBO.getTransAmount());
        transferOrderFlowDO.setStatus(transferFlowStatus);
        transferOrderFlowDO.setPayFinishTime(queryResultBO.getFinishTime());
        if (queryResultBO.getFinishTime() != null) {
            transferOrderFlowDO.setPayFinishDate(DateUtils.format(queryResultBO.getFinishTime(),
                    DateUtils.DATE));
        }
        transferOrderFlowDO.setFailReason(queryResultBO.getErrorMsg());
        return transferOrderFlowDO;
    }



    private AddMoneyFlowMessage buildAddMoneyFlowMessage(TransferOrderDO oned, BigDecimal incomeMoney) {

        RecordTypeEnum recordType = null;
        TransferEventTypeEnum eventType = oned.getEventType();
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
        addMoneyFlowMessage.setBusinessId(oned.getMerchantId());
        addMoneyFlowMessage.setBizOrderId(oned.getBizOrderId());
        addMoneyFlowMessage.setFlowNo(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.MONEY_FLOW, oned.getOutTradeNo()));
        addMoneyFlowMessage.setRecordType(recordType);
        addMoneyFlowMessage.setIncomeMoney(incomeMoney);
        return addMoneyFlowMessage;
    }




}
