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

/**
 * 支付订单补偿
 */
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

        handleCompensatePayment(message);

        log.info("处理补偿支付订单完成");

    }

    private void handleCompensatePayment(CompensatePaymentOrderMessage message) {

        String outTradeNo = message.getOutTradeNo();
        Integer payType = message.getPayType();
        Integer retryLevel = message.getRetryLevel();
        Boolean bathOrder = message.getBathOrder();

        log.info("查询三方支付订单状态开始");

        QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatusBO = queryThirdPartyPaymentStatus(outTradeNo, payType, bathOrder);
        CompensatePaymentStatusEnum paymentSuccess = queryThirdPartyPaymentStatusBO.getPaymentStatus();
        TradeStatusBO tradeStatusBO = queryThirdPartyPaymentStatusBO.getTradeStatusBO();

        log.info("查询三方支付订单状态完成");


        switch (paymentSuccess) {

            case CONTINUE:
                log.info("重新投递补偿消息");
                scheduleNextRetry(outTradeNo, payType, retryLevel, bathOrder);
                break;

            case SUCCESS:
                log.info("三方支付成功,回调处理开始");
                boolean executed = executePaymentCompensation(payType, tradeStatusBO);
                if (!executed) {
                    log.info("三方支付成功,回调处理失败,继续重试");
                    scheduleNextRetry(outTradeNo, payType, retryLevel, bathOrder);
                    return;
                }
                log.info("三方支付成功,回调处理完成");
                break;
            case FINISH:
                log.info("已完成支付成功处理,结束补投任务");
                break;
        }

    }


    private QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatus(String outTradeNo,
                                                                        Integer payType, Boolean bathOrder) {


        if (bathOrder) {

            log.info("批量支付单查询前业务校验");

            BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                    .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (bathTradeOrderDO == null) {
                log.error("批量交易订单不存在");
                throw new BizException("批量交易订单不存在");
            }
            if (!BathTradeOrderStatusEnum.PENDING.getCode().equals(bathTradeOrderDO.getPayStatus())) {
                log.warn("批量交易单状态非待支付状态");
                return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
            }

        } else {

            log.info("单笔支付单查询前业务校验");

            TradeOrderDO tradeOrder = tradeOrderService.lambdaQuery()
                    .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();

            if (tradeOrder == null) {
                log.error("交易订单不存在" );
                throw new BizException("交易订单不存在");
            }

            if (!TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrder.getPayStatus())) {

                log.warn("交易单状态非待支付状态");

                return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
            }

        }

        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        log.info("当前三方支付处理类为:{}", paymentService.getClass().getName());

        TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);

        TradeStatusEnum tradeStatus = tradeStatusBO.getTradeStatus();
        log.info("查询三方支付订单交易状态为:{}", tradeStatus);

        CompensatePaymentStatusEnum paymentStatus = null;
        switch (tradeStatus) {
            case TRADE_SUCCESS:

                log.info("三方支付成功");
                paymentStatus = CompensatePaymentStatusEnum.SUCCESS;
                break;
            case UNKNOWN:
                /**
                 * 可能丢单 即没有流水记录 取消交易单时 插入流水记录
                 */
                log.warn("支付订单未知状态");
                paymentStatus = CompensatePaymentStatusEnum.CONTINUE;
                break;

        }
        return new QueryThirdPartyPaymentStatusBO(paymentStatus, tradeStatusBO);
    }


    private boolean executePaymentCompensation( Integer payType, TradeStatusBO tradeStatusBO) {

        PaySuccessHandleBO successHandleBO = buildPaySuccessHandleBO(payType, tradeStatusBO);
        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        PaySuccessHandleResultBO resultBO = paymentService.paySuccessHandleBO(successHandleBO);

        if (!resultBO.getHandleSuccess()) {

            log.warn("支付成功回调处理失败");
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

    private void scheduleNextRetry(String outTradeNo, Integer payType, Integer currentRetryLevel, Boolean bathOrder) {

        int nextRetryLevel = currentRetryLevel + 1;


        PayCompensateOrderRetryPolicyBO matchOrNext = findMatchOrNext(nextRetryLevel);
        if (matchOrNext == null) {
            log.error("超过最大重试次数currentRetryLevel:{},nextRetryLevel:{}", currentRetryLevel,nextRetryLevel);
            return;
        }

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

        return exactMatch.orElse(null);

    }


}
