package com.lanf.pay.mq;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.CompensatePaymentStatusEnum;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.model.enums.TradeStatusEnum;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
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

import java.util.Comparator;
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
        }
    }

    private void handleCompensatePayment(CompensatePaymentOrderMessage message) {

        String outTradeNo = message.getOutTradeNo();
        String payType = message.getPayType();
        Integer retryLevel = message.getRetryLevel();
        QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatusBO = queryThirdPartyPaymentStatus(outTradeNo, payType);
        CompensatePaymentStatusEnum paymentSuccess = queryThirdPartyPaymentStatusBO.getPaymentStatus() ;
        switch (paymentSuccess){

            case CONTINUE:
                scheduleNextRetry( outTradeNo,  payType,retryLevel);
                break;

            case SUCCESS:
                executePaymentCompensation(outTradeNo, payType, queryThirdPartyPaymentStatusBO.getTradeStatusBO());
                break;
            case FINISH:
                log.info("支付成功,结束补投任务");
                break;
        }

    }



    private QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatus(String outTradeNo, String payType) {

        TradeOrderDO tradeOrder = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (tradeOrder == null) {
            log.error("交易订单不存在:outTradeNo={}", outTradeNo);
            throw new BizException("交易订单不存在");
        }

        if (!TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrder.getPayStatus())) {
            log.info("订单非待支付状态，结束补投:outTradeNo={},payStatus={}", outTradeNo, tradeOrder.getPayStatus());
            return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
        }

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(Integer.parseInt(payType));
        TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);
        TradeStatusEnum tradeStatus = tradeStatusBO.getTradeStatus();

        CompensatePaymentStatusEnum paymentStatus;
        switch (tradeStatus) {
            case TRADE_SUCCESS:
                log.info("三方支付成功，准备执行补偿:outTradeNo={},payType={}", outTradeNo, payType);
                paymentStatus = CompensatePaymentStatusEnum.SUCCESS;
                break;
            case WAIT_BUYER_PAY:
            case NOT_EXIST:
            case UNKNOWN:
                log.info("订单待支付，等待支付完成:outTradeNo={},payType={}", outTradeNo, payType);
                paymentStatus = CompensatePaymentStatusEnum.CONTINUE;
                break;
            case TRADE_FINISHED:
            case TRADE_CLOSED:
                log.info("订单支付完成，结束补投:outTradeNo={},payType={}", outTradeNo, payType);
                paymentStatus = CompensatePaymentStatusEnum.FINISH;
                break;
            default:
                paymentStatus = CompensatePaymentStatusEnum.CONTINUE;
                break;
        }

        return new QueryThirdPartyPaymentStatusBO(paymentStatus, tradeStatusBO);
    }

    private void executePaymentCompensation(String outTradeNo, String payType,TradeStatusBO tradeStatusBO) {


        PaySuccessHandleBO successHandleBO = BeanCopyUtils.copyBean(tradeStatusBO, PaySuccessHandleBO.class);
        PaySuccessHandleResultBO resultBO = tradeOrderService.paySuccessHandleBO(successHandleBO);
        if ( !resultBO.getHandleSuccess()) {
            log.warn("支付成功处理失败:outTradeNo={},payType={}", outTradeNo, payType);
            throw new BizException("支付成功处理失败");
        }

    }

    private void scheduleNextRetry(String outTradeNo, String payType, Integer currentRetryLevel) {

        int nextRetryLevel = currentRetryLevel + 1;
        log.info("安排下次重试:outTradeNo={},payType={},nextRetryLevel={}",
                outTradeNo, payType, nextRetryLevel);

        PayCompensateOrderRetryPolicyBO matchOrNext = findMatchOrNext(nextRetryLevel);
        CompensatePaymentOrderMessage message = buildCompensatePaymentMessage(outTradeNo, payType, matchOrNext.getRetryLevel());
        rocketMqClient.sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                JsonUtils.toJsonString(message), TimeUnit.SECONDS, matchOrNext.getDelaySeconds());

        
    }


    public PayCompensateOrderRetryPolicyBO findMatchOrNext(int targetLevel) {

        List<PayCompensateOrderRetryPolicyBO> list = payRetryPolicyCacheService.getAllRetryPolicies();
        // 1. 查找 retryLevel 相等的元素
        Optional<PayCompensateOrderRetryPolicyBO> exactMatch = list.stream()
                .filter(p -> p.getRetryLevel() == targetLevel)
                .findFirst();

        // // 2. 查找比 targetLevel 大的最小 retryLevel 的元素 如果没有更大的级别，返回 null
        return exactMatch.orElseGet(() -> list.stream()
                .filter(p -> p.getRetryLevel() > targetLevel)
                .min(Comparator.comparingInt(PayCompensateOrderRetryPolicyBO::getRetryLevel))
                .orElse(null));


    }
    private CompensatePaymentOrderMessage buildCompensatePaymentMessage(String outTradeNo, String payType,Integer retryLevel) {
        CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
        message.setOutTradeNo(outTradeNo);
        message.setPayType(payType);
        message.setRetryLevel(retryLevel);

        return message;
    }
}
