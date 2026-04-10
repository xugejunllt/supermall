package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.entity.PayCompensateOrderRetryPolicy;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.IPayCompensateOrderRetryPolicyService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OrderCreateSuccessMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RocketMQMessageListener(
    topic = TopicName.ORDER_CREATE_SUCCESS_TOPIC, 
    consumerGroup = TopicName.ORDER_CREATE_PAY_GROUP
)
public class OrderCreateSuccessListener implements RocketMQListener<OrderCreateSuccessMessage> {

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IPayCompensateOrderRetryPolicyService retryPolicyService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(OrderCreateSuccessMessage message) {

        log.info("收到订单创建成功通知:[{}]",JsonUtils.toJsonString(message));
        
        try {
            handleOrderCreateSuccess(message);
            log.info("处理订单创建成功通知完成:orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.error("处理订单创建成功通知失败:orderId={}", message.getOrderId(), e);

        }
    }

    private void handleOrderCreateSuccess(OrderCreateSuccessMessage message) {
        
        String outTradeNo = queryOutTradeNoByOrderId(message.getOrderId());
        
        if (outTradeNo == null) {
            log.error("未查询到交易单信息:orderId={}", message.getOrderId());
            return;
        }
        
        checkThirdPartyPaymentStatus(message, outTradeNo);
    }

    private String queryOutTradeNoByOrderId(Long orderId) {
        
        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOrderId, orderId)
                .one();
        
        if (tradeOrderDO == null) {
            log.warn("交易单不存在:orderId={}", orderId);
            return null;
        }
        
        return tradeOrderDO.getOutTradeNo();
    }

    private void checkThirdPartyPaymentStatus(OrderCreateSuccessMessage message, String outTradeNo) {
        
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(message.getPayType());
        
        // TODO: 这里调用第三方支付查询接口，你自己补充具体实现
        // 示例：Integer payStatus = paymentService.queryPaymentStatus(outTradeNo);
        Integer payStatus = queryPaymentStatusFromThirdParty(paymentService, outTradeNo);
        
        if (payStatus == null) {
            log.warn("查询第三方支付状态返回null:orderId={},outTradeNo={}", 
                    message.getOrderId(), outTradeNo);
            scheduleRetryCheck(message, outTradeNo);
            return;
        }
        
        if (TradeOrderStatusEnum.PENDING.getCode().equals(payStatus)) {
            log.info("订单仍为待支付状态，安排延迟检查:orderId={},outTradeNo={}", 
                    message.getOrderId(), outTradeNo);
            scheduleRetryCheck(message, outTradeNo);
        } else if (TradeOrderStatusEnum.COMPLETED.getCode().equals(payStatus)) {
            log.info("订单已支付:orderId={},outTradeNo={}", 
                    message.getOrderId(), outTradeNo);
        } else if (TradeOrderStatusEnum.CANCELLED.getCode().equals(payStatus)) {
            log.info("订单已取消:orderId={},outTradeNo={}", 
                    message.getOrderId(), outTradeNo);
        } else {
            log.warn("未知的支付状态:orderId={},outTradeNo={},payStatus={}", 
                    message.getOrderId(), outTradeNo, payStatus);
        }
    }

    private Integer queryPaymentStatusFromThirdParty(PaymentService paymentService, String outTradeNo) {
        
        // TODO: 你自己补充具体的三方支付查询逻辑
        // 这里提供空方法框架
        return null;
    }

    private void scheduleRetryCheck(OrderCreateSuccessMessage message, String outTradeNo) {
        
        Integer currentRetryCount = message.getRetryCount() == null ? 0 : message.getRetryCount();
        
        PayCompensateOrderRetryPolicy retryPolicy = getRetryPolicy(currentRetryCount + 1);
        
        if (retryPolicy == null || retryPolicy.getIsEnabled() == 1) {
            log.error("已达到最大重试次数或重试策略已禁用，停止重试:orderId={},retryCount={}", 
                    message.getOrderId(), currentRetryCount);
            return;
        }
        
        message.setRetryCount(currentRetryCount + 1);
        message.setLastCheckTime(System.currentTimeMillis());
        
        int delaySeconds = retryPolicy.getDelaySeconds();
        
        log.info("发送延迟检查消息:orderId={},retryCount={},delaySeconds={}", 
                message.getOrderId(), message.getRetryCount(), delaySeconds);
        
        rocketMqClient.sendDelayMessage(
                TopicName.ORDER_PAY_STATUS_CHECK_TOPIC, 
                message, 
                delaySeconds
        );
    }

    private PayCompensateOrderRetryPolicy getRetryPolicy(int retryLevel) {
        
        List<PayCompensateOrderRetryPolicy> policies = retryPolicyService.lambdaQuery()
                .eq(PayCompensateOrderRetryPolicy::getRetryLevel, retryLevel)
                .eq(PayCompensateOrderRetryPolicy::getIsEnabled, 0)
                .list();
        
        if (policies == null || policies.isEmpty()) {
            return null;
        }
        
        return policies.get(0);
    }
}
