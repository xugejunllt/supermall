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
 * 支付订单补偿监听器
 * <p>
 * 负责监听支付补偿消息队列，查询第三方支付平台订单状态，根据查询结果进行补偿处理。
 * 支持批量订单和单笔订单两种模式，通过延迟消息实现阶梯式重试策略。
 * </p>
 *
 * <p><b>核心流程：</b></p>
 * <ol>
 *   <li>接收补偿消息</li>
 *   <li>查询三方支付订单实际状态</li>
 *   <li>根据状态决策：继续重试 / 执行补偿 / 结束任务</li>
 * </ol>
 *
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>基于MQ延迟消息实现指数退避重试</li>
 *   <li>状态机驱动补偿流程（CONTINUE / SUCCESS / FINISH）</li>
 *   <li>工厂模式获取对应支付渠道的Service</li>
 * </ul>
 *
 * @author system
 * @since 2024-01-01
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

    /**
     * 消费补偿支付订单消息
     *
     * @param message 补偿支付订单消息体，包含订单号、支付类型、重试等级等
     */
    @Override
    public void onMessage(CompensatePaymentOrderMessage message) {

        log.info("收到补偿支付订单消息:[{}]", JsonUtils.toJsonString(message));

        handleCompensatePayment(message);

        log.info("处理补偿支付订单完成");

    }

    /**
     * 处理补偿支付逻辑
     * <p>
     * 核心调度方法，负责查询三方支付状态并根据结果进行路由处理：
     * <ul>
     *   <li>支付中/未知 → 继续重试</li>
     *   <li>支付成功 → 执行本地补偿逻辑</li>
     *   <li>已结束/已处理 → 终止任务</li>
     * </ul>
     * </p>
     *
     * @param message 补偿消息
     */
    private void handleCompensatePayment(CompensatePaymentOrderMessage message) {

        String outTradeNo = message.getOutTradeNo();
        Integer payType = message.getPayType();
        Integer retryLevel = message.getRetryLevel();
        Boolean bathOrder = message.getBathOrder();

        log.info("查询三方支付订单状态开始");

        // 查询三方平台实际支付状态
        QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatusBO = queryThirdPartyPaymentStatus(outTradeNo, payType, bathOrder);
        CompensatePaymentStatusEnum paymentSuccess = queryThirdPartyPaymentStatusBO.getPaymentStatus();
        TradeStatusBO tradeStatusBO = queryThirdPartyPaymentStatusBO.getTradeStatusBO();

        log.info("查询三方支付订单状态完成");


        switch (paymentSuccess) {

            case CONTINUE:
                // 支付状态未知或仍在处理中，需要继续轮询
                log.info("重新投递补偿消息");
                scheduleNextRetry(outTradeNo, payType, retryLevel, bathOrder);
                break;

            case SUCCESS:
                // 三方已支付成功，执行本地业务补偿（更新订单状态、通知下游等）
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
                // 订单已完成处理或处于终态，无需再处理
                log.info("已完成支付成功处理,结束补投任务");
                break;
        }

    }


    /**
     * 查询第三方支付平台的订单状态
     * <p>
     * 先进行本地业务校验（订单是否存在、状态是否待支付），
     * 再通过对应支付渠道的SDK查询实际交易状态。
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @param payType    支付方式类型
     * @param bathOrder  是否为批量订单
     * @return 查询结果BO，包含补偿状态枚举和三方交易状态
     */
    private QueryThirdPartyPaymentStatusBO queryThirdPartyPaymentStatus(String outTradeNo,
                                                                        Integer payType, Boolean bathOrder) {


        if (bathOrder) {

            log.info("批量支付单查询前业务校验");

            // 校验批量交易订单是否存在且处于待支付状态
            BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                    .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (bathTradeOrderDO == null) {
                log.error("批量交易订单不存在");
                throw new BizException("批量交易订单不存在");
            }
            // 非待支付状态说明已处理过，返回FINISH避免重复补偿
            if (!BathTradeOrderStatusEnum.PENDING.getCode().equals(bathTradeOrderDO.getPayStatus())) {
                log.warn("批量交易单状态非待支付状态");
                return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
            }

        } else {

            log.info("单笔支付单查询前业务校验");

            // 校验单笔交易订单是否存在且处于待支付状态
            TradeOrderDO tradeOrder = tradeOrderService.lambdaQuery()
                    .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();

            if (tradeOrder == null) {
                log.error("交易订单不存在");
                throw new BizException("交易订单不存在");
            }

            // 非待支付状态说明已处理过，返回FINISH避免重复补偿
            if (!TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrder.getPayStatus())) {

                log.warn("交易单状态非待支付状态");

                return new QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum.FINISH, null);
            }

        }

        // 通过工厂获取对应支付类型的Service（如支付宝、微信等）
        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        log.info("当前三方支付处理类为:{}", paymentService.getClass().getName());

        // 调用三方SDK查询真实交易状态
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
            case TRADE_FINISHED:

                log.warn("三方交易已结束");
                paymentStatus = CompensatePaymentStatusEnum.FINISH;
                break;
        }

        return new QueryThirdPartyPaymentStatusBO(paymentStatus, tradeStatusBO);
    }


    /**
     * 执行支付成功后的本地补偿逻辑
     * <p>
     * 当确认三方已支付成功后，调用对应支付渠道Service处理本地业务：
     * 更新订单状态、记录流水、发送通知等。
     * </p>
     *
     * @param payType      支付方式类型
     * @param tradeStatusBO 三方交易状态信息
     * @return true-补偿成功; false-补偿失败，需要重试
     */
    private boolean executePaymentCompensation(Integer payType, TradeStatusBO tradeStatusBO) {

        PaySuccessHandleBO successHandleBO = buildPaySuccessHandleBO(payType, tradeStatusBO);
        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        PaySuccessHandleResultBO resultBO = paymentService.paySuccessHandleBO(successHandleBO);

        if (!resultBO.getHandleSuccess()) {

            log.warn("支付成功回调处理失败");
            return false;
        }
        return true;
    }

    /**
     * 构建支付成功处理BO
     * <p>
     * 将三方返回的交易状态信息转换为本地的支付成功处理对象，
     * 供后续业务逻辑统一处理。
     * </p>
     *
     * @param payType      支付方式类型
     * @param tradeStatusBO 三方交易状态信息
     * @return 支付成功处理BO
     */
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

    /**
     * 调度下一次重试
     * <p>
     * 根据当前重试等级查找下一级重试策略，通过MQ延迟消息实现定时重试。
     * 如果已超过最大重试次数，则放弃重试并记录日志。
     * </p>
     *
     * @param outTradeNo      商户订单号
     * @param payType         支付方式类型
     * @param currentRetryLevel 当前重试等级
     * @param bathOrder       是否为批量订单
     */
    private void scheduleNextRetry(String outTradeNo, Integer payType, Integer currentRetryLevel, Boolean bathOrder) {

        int nextRetryLevel = currentRetryLevel + 1;


        PayCompensateOrderRetryPolicyBO matchOrNext = findMatchOrNext(nextRetryLevel);
        if (matchOrNext == null) {
            log.error("超过最大重试次数currentRetryLevel:{},nextRetryLevel:{}", currentRetryLevel, nextRetryLevel);
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


    /**
     * 查找匹配的重试策略
     * <p>
     * 从重试策略缓存中查找与目标等级匹配的策略配置。
     * 策略配置通常包含重试等级和对应的延迟时间（如1级延迟5秒、2级延迟30秒等）。
     * </p>
     *
     * @param targetLevel 目标重试等级
     * @return 匹配的重试策略，如果不存在则返回null（表示已超过最大重试次数）
     */
    public PayCompensateOrderRetryPolicyBO findMatchOrNext(int targetLevel) {

        List<PayCompensateOrderRetryPolicyBO> list = payRetryPolicyCacheService.getAllRetryPolicies();
        // 1. 查找 retryLevel 相等的元素
        Optional<PayCompensateOrderRetryPolicyBO> exactMatch = list.stream()
                .filter(p -> p.getRetryLevel() == targetLevel)
                .findFirst();

        return exactMatch.orElse(null);

    }


}
