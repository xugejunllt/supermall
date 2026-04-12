package com.lanf.pay.mq;

import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.PayCompensateOrderRetryPolicyBO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.service.trade.IPayCompensateOrderRetryPolicyService;
import com.lanf.pay.service.trade.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.trade.impl.PayRetryPolicyCacheService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CompensatePaymentOrderMessage;
import com.lanf.rocketmq.model.message.OrderCreateSuccessMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;


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
    @Autowired
    private IPrepayPayTypeService prepayPayTypeService;
    @Autowired
    private PayRetryPolicyCacheService payRetryPolicyCacheService;
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
        
        List<String> prepayPayTypesByOutTradeNo = prepayPayTypeService.getPrepayPayTypesByOutTradeNo(outTradeNo);
        if (IStringUtils.isEmpty(prepayPayTypesByOutTradeNo)) {
            log.info("未查询到支付类型:orderId={}", message.getOrderId());
            return;
        }

        sendCompensatePaymentMessages(outTradeNo, prepayPayTypesByOutTradeNo);
    }

    private void sendCompensatePaymentMessages(String outTradeNo, List<String> payTypes) {

        for (String payType : payTypes) {
            PayCompensateOrderRetryPolicyBO firstLevelRetryPolicy = payRetryPolicyCacheService.getFirstLevelRetryPolicy();

            CompensatePaymentOrderMessage message = buildCompensatePaymentMessage(outTradeNo, payType, firstLevelRetryPolicy.getRetryLevel());
            rocketMqClient. sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                    JsonUtils.toJsonString(message), TimeUnit.SECONDS, firstLevelRetryPolicy.getDelaySeconds());

        }
        
        log.info("批量发送补偿支付消息完成:outTradeNo={},payTypeCount={}", outTradeNo, payTypes.size());
    }

    private CompensatePaymentOrderMessage buildCompensatePaymentMessage(String outTradeNo, String payType,Integer retryLevel) {
        CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
        message.setOutTradeNo(outTradeNo);
        message.setPayType(payType);
        message.setRetryLevel(retryLevel);

        return message;
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

}
