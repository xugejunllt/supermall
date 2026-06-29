package com.lanf.pay.mq.listener;

import com.lanf.pay.model.bo.ReconciliationStart;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.pay.service.reconciliation.strategy.ReconciliationStrategyFactory;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    @Autowired
    private IReconciliationDiffMarkerService reconciliationDiffMarkerService;


    @Autowired
    private ReconciliationStrategyFactory reconciliationStrategyFactory;
    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(ReconciliationStartMessage message) {

        log.info("开始对账任务 {}", message);
        ReconciliationJobTypeEnum jobType = message.getJobType();

        ReconciliationStart start = new ReconciliationStart();
        start.setBathId(message.getBathId());
        start.setBathMaxId(message.getBathMaxId());
        start.setReconciliationBusinessType(message.getReconciliationBusinessType());
        start.setReconciliationTradeInfoList(message.getReconciliationTradeInfoList());
        reconciliationStrategyFactory.getStrategy(jobType).startReconciliation(start);


    }




}
