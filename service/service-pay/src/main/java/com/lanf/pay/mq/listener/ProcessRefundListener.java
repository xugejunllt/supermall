package com.lanf.pay.mq.listener;

import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.ProcessRefund;
import com.lanf.client.pay.mq.message.ProcessRefundMessage;
import com.lanf.pay.service.pay.IRefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理退款消息消费者
 * 全部退款 原路返回
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.PROCESS_REFUND_TOPIC,
        consumerGroup = PayClientTopicName.PROCESS_REFUND_PAY_GROUP
)
public class ProcessRefundListener implements RocketMQListener<ProcessRefundMessage> {

    @Autowired
    private IRefundOrderService refundOrderService;

    @Override
    public void onMessage(ProcessRefundMessage message) {

        log.info("收到退款消息:[{}]", JsonUtils.toJsonString(message));
        ProcessRefund processRefund = new ProcessRefund();
        processRefund.setBizOrderId(message.getBizOrderId());
        processRefund.setOutTradeNo(message.getOutTradeNo());
        processRefund.setOutRequestNo(message.getOutRequestNo());
        processRefund.setPayType(message.getPayType());
        processRefund.setRefundEventTypeEnum(message.getRefundEventTypeEnum());

        refundOrderService.processRefund(processRefund);
        log.info("处理退款完成");

    }

}
