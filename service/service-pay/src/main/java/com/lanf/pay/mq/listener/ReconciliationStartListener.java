package com.lanf.pay.mq.listener;

import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    @Override
    public void onMessage(ReconciliationStartMessage message) {





    }




}
