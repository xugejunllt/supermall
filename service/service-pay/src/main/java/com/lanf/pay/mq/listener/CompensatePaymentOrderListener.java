package com.lanf.pay.mq.listener;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.entity.BathTradeOrderDO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.BathTradeOrderStatusEnum;
import com.lanf.pay.model.enums.CompensatePaymentStatusEnum;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.model.enums.TradeStatusEnum;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.IBathTradeOrderService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.trade.impl.PayRetryPolicyCacheService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CompensatePaymentOrderMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RocketMQMessageListener(
    topic = TopicName.COMPENSATE_PAYMENT_TOPIC, 
    consumerGroup = TopicName.COMPENSATE_PAYMENT_GROUP
)
public class CompensatePaymentOrderListener implements RocketMQListener<CompensatePaymentOrderMessage> {


    @Autowired
    private PayRetryPolicyCacheService payRetryPolicyCacheService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IBathTradeOrderService bathTradeOrderService;
    @Autowired
    private PaymentServiceFactory paymentServiceFactory;

    @Override
    public void onMessage(CompensatePaymentOrderMessage message) {

        log.info("收到补偿支付订单消息:[{}]", JsonUtils.toJsonString(message));
        
        try {
            handleCompensatePayment(message);
            log.info("处理补偿支付订单完成:outTradeNo={},payType={}", 
                    message.getOutTradeNo(), message.getPayType());
        } catch (Exception e) {
            log.error("处理补偿支付订单失败:outTradeNo={},payType={}", 
                    message.getOutTradeNo(), message.getPayType(), e);
            throw e;
        }
    }

    private void handleCompensatePayment(CompensatePaymentOrderMessage message) {

        String outTradeNo = message.getOutTradeNo();
        Integer payType = message.getPayType();
        Integer retryLevel = message.getRetryLevel();
        Boolean bathOrder = message.getBathOrder();
        QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatusBO = queryThirdPartyPaymentStatus(outTradeNo, payType,bathOrder);
        CompensatePaymentStatusEnum paymentSuccess = queryThirdPartyPaymentStatusBO.getPaymentStatus() ;
        TradeStatusBO tradeStatusBO = queryThirdPartyPaymentStatusBO.getTradeStatusBO();
        switch (paymentSuccess){

            case CONTINUE:
                scheduleNextRetry( outTradeNo,  payType,retryLevel, bathOrder);
                break;

            case SUCCESS:

                boolean executed = executePaymentCompensation(outTradeNo, payType, tradeStatusBO);
                if (!executed) {
                    scheduleNextRetry( outTradeNo,  payType,retryLevel, bathOrder);
                    return;
                }
                break;
            case FINISH:
                log.info("支付成功,结束补投任务");
                break;
        }

    }



    private QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatus(String outTradeNo,
                                                                        Integer payType,Boolean bathOrder) {



        if (bathOrder){


            BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                    .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (bathTradeOrderDO == null) {
                log.warn("批量交易订单不存在:outTradeNo={}", outTradeNo);
                throw new BizException("批量交易订单不存在");
            }

            if (!BathTradeOrderStatusEnum.PENDING.getCode().equals(bathTradeOrderDO.getPayStatus())) {
                log.info("订单非待支付状态，结束补投:outTradeNo={},payStatus={}", outTradeNo, bathTradeOrderDO.getPayStatus());
                return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
            }

        } else {
            TradeOrderDO tradeOrder = tradeOrderService.lambdaQuery()
                    .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();

            if (tradeOrder == null) {
                log.warn("交易订单不存在:outTradeNo={}", outTradeNo);
                throw new BizException("交易订单不存在");
            }

            if (!TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrder.getPayStatus())) {
                log.info("订单非待支付状态，结束补投:outTradeNo={},payStatus={}", outTradeNo, tradeOrder.getPayStatus());
                return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
            }
        }

        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);

        TradeStatusEnum tradeStatus = tradeStatusBO.getTradeStatus();
        log.info("支付宝交易状态:{}", tradeStatus);
        CompensatePaymentStatusEnum paymentStatus = null;
        switch (tradeStatus) {
            case TRADE_SUCCESS:

                log.info("三方支付成功，准备执行补偿:outTradeNo={},payType={}", outTradeNo, payType);
                paymentStatus = CompensatePaymentStatusEnum.SUCCESS;
                break;
            case UNKNOWN:
                /**
                 * 可能丢单 即没有流水记录 取消交易单时 插入流水记录
                 */
                paymentStatus = CompensatePaymentStatusEnum.CONTINUE;
                break;

        }
        return new QueryThirdPartyPaymentStatusBO(paymentStatus, tradeStatusBO);
    }



    private boolean executePaymentCompensation(String outTradeNo, Integer payType,TradeStatusBO tradeStatusBO) {

        PaySuccessHandleBO successHandleBO = buildPaySuccessHandleBO( payType,tradeStatusBO);
        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        PaySuccessHandleResultBO resultBO = paymentService.paySuccessHandleBO(successHandleBO);
        if ( !resultBO.getHandleSuccess()) {
            log.warn("支付成功处理失败:outTradeNo={},payType={}", outTradeNo, payType);
            return false;
        }
        return true;
    }

    private PaySuccessHandleBO buildPaySuccessHandleBO(Integer payType, TradeStatusBO tradeStatusBO) {
        CallbackResultBO callbackResultBO = new CallbackResultBO();
        callbackResultBO.setPayFinishTime(tradeStatusBO.getPayFinishTime());
        callbackResultBO.setReceiptMoney(tradeStatusBO.getReceiptMoney());
        callbackResultBO.setTotalAmount(tradeStatusBO.getTotalAmount());
        callbackResultBO.setPayAccount(tradeStatusBO.getPayAccount());
        callbackResultBO.setIncomeAccount(tradeStatusBO.getIncomeAccount());
        callbackResultBO.setNotifyTime(tradeStatusBO.getNotifyTime());
        callbackResultBO.setTradeNo(tradeStatusBO.getTradeNo());
        callbackResultBO.setOutTradeNo(tradeStatusBO.getOutTradeNo());
        callbackResultBO.setStrPassbackParams(tradeStatusBO.getStrPassbackParams());
        callbackResultBO.setAllParams(tradeStatusBO.getAllParams());

        PaySuccessHandleBO successHandleBO = new PaySuccessHandleBO();
        successHandleBO.setPayType(payType);
        successHandleBO.setResultBO(callbackResultBO);

        return successHandleBO;
    }
    private void scheduleNextRetry(String outTradeNo, Integer payType, Integer currentRetryLevel,Boolean bathOrder) {

        int nextRetryLevel = currentRetryLevel + 1;


        PayCompensateOrderRetryPolicyBO matchOrNext = findMatchOrNext(nextRetryLevel);
        if (matchOrNext == null){
            log.error("超过最大重试次数outTradeNo:outTradeNo{},nextRetryLevel:{}",outTradeNo,nextRetryLevel);
            return;
        }
        log.info("安排下次重试:outTradeNo={},payType={},nextRetryLevel={}",
                outTradeNo, payType, nextRetryLevel);
        CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
        message.setOutTradeNo(outTradeNo);
        message.setPayType(payType);
        message.setRetryLevel(matchOrNext.getRetryLevel());
        message.setBathOrder(bathOrder);
        rocketMqClient.sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                JsonUtils.toJsonString(message), TimeUnit.SECONDS, matchOrNext.getDelaySeconds());

        
    }


    public PayCompensateOrderRetryPolicyBO findMatchOrNext(int targetLevel) {

        List<PayCompensateOrderRetryPolicyBO> list = payRetryPolicyCacheService.getAllRetryPolicies();
        // 1. 查找 retryLevel 相等的元素
        Optional<PayCompensateOrderRetryPolicyBO> exactMatch = list.stream()
                .filter(p -> p.getRetryLevel() == targetLevel)
                .findFirst();
        log.info("查找 retryLevel 相等的元素:list{},targetLevel{}", list,targetLevel);

        return exactMatch.orElse(null);

    }







}
