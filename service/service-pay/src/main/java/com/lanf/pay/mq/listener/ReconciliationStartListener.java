package com.lanf.pay.mq.listener;

import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 开始对账任务
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.RECONCILIATION_START_TOPIC,
        consumerGroup = PayMqGroupName.RECONCILIATION_START_GROUP
)
public class ReconciliationStartListener implements RocketMQListener<ReconciliationStartMessage> {



    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(ReconciliationStartMessage message) {





    }


}
