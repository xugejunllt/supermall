package com.lanf.pay.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.CancelWaitPayOrderBO;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelWaitPayOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 交易成功事件 - 支付服务消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = TopicName.CANCEL_WAIT_PAY_ORDER_TOPIC,
    consumerGroup = TopicName.CANCEL_WAIT_PAY_ORDER_GROUP
)
public class CancelWaitPayOrderListener implements RocketMQListener<CancelWaitPayOrderMessage> {

    @Autowired
    private IPaymentCancelRecordService paymentCancelRecordService;

    @Override
    public void onMessage(CancelWaitPayOrderMessage message) {

        log.info("取消待支付订单开始:[{{}}]", JsonUtils.toJsonString(message));
        CancelWaitPayOrderBO cancelWaitPayOrderBO = new CancelWaitPayOrderBO();
        cancelWaitPayOrderBO.setOutTradeNo(message.getOutTradeNo());
        cancelWaitPayOrderBO.setPayType(message.getPayType());
        cancelWaitPayOrderBO.setCancelSource(message.getCancelSource());
        cancelWaitPayOrderBO.setCurrentPayStatus(message.getCurrentPayStatus());
        paymentCancelRecordService.cancelWaitPayOrder(cancelWaitPayOrderBO);


    }
}
