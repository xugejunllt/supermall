package com.lanf.logistics.mq.event;


import com.lanf.logistics.service.ILogisticsService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.PAY_SUCCESS_EVENT_TOPIC, consumerGroup = TopicName.PAY_SUCCESS_LOGISTICS_EVENT_GROUP)
public class PaySuccessLogisticsEventListener implements RocketMQListener<PaySuccessEventMessage> {

    @Autowired
    private ILogisticsService logisticsService;
    @Override
    public void onMessage(PaySuccessEventMessage paySuccessEventMessage) {
        log.info("支付成功,物流业务操作");
        logisticsService.paySuccessHandle(paySuccessEventMessage);

    }

}
