package com.lanf.finance.mq;

import com.lanf.finance.service.IContrastBillService;
import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.ContrastBillTaskMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.CONTRAST_BILL_TASK_TOPIC, consumerGroup = TopicName.CONTRAST_BILL_TASK_GROUP)
public class ContrastBillTaskListener implements RocketMQListener<ContrastBillTaskMsg> {

    @Autowired
    private IContrastBillService contrastBillService;
    @ConsumeMessage
    @Override
    public void onMessage(ContrastBillTaskMsg message) {

        log.info("对账任务开始:{}", message);

        try {
            contrastBillService.startContrastBillTask(message.getOrderId());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}