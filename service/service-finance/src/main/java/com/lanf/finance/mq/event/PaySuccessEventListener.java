package com.lanf.finance.mq.event;


import com.lanf.finance.service.ILiquidationService;
import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.PAY_SUCCESS_EVENT_TOPIC, consumerGroup = TopicName.PAY_SUCCESS_FINANCE_EVENT_GROUP)
public class PaySuccessEventListener implements RocketMQListener<PaySuccessEventMessage> {

    @Autowired
    private ILiquidationService liquidationService;
    @ConsumeMessage
    @Override
    public void onMessage(PaySuccessEventMessage paySuccessEventMessage) {
        log.info("进行结算:{}", paySuccessEventMessage.getSettlementDTO());
        liquidationService.createLiquidation(paySuccessEventMessage.getSettlementDTO());

    }

}
