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
        

    }



}
