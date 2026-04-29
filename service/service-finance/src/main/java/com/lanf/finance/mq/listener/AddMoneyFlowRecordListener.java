package com.lanf.finance.mq.listener;

import com.lanf.finance.model.bo.AddMoneyFlow;
import com.lanf.finance.mq.constant.FinanceClientTopicName;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.finance.service.IMoneyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
    topic = FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC,
    consumerGroup = FinanceClientTopicName.MONEY_FLOW_RECORD_GROUP
)
public class AddMoneyFlowRecordListener implements RocketMQListener<AddMoneyFlowMessage> {

    @Autowired
    private IMoneyFlowService moneyFlowService;

    @Override
    public void onMessage(AddMoneyFlowMessage message) {

        AddMoneyFlow addMoneyFlow = new AddMoneyFlow();
        addMoneyFlow.setBusinessId(message.getBusinessId());
        addMoneyFlow.setBizOrderId(message.getBizOrderId());
        addMoneyFlow.setRecordType(message.getRecordType());
        addMoneyFlow.setIncomeMoney(message.getIncomeMoney());
        addMoneyFlow.setFlowNo(message.getFlowNo());
        moneyFlowService.addMoneyFlow(addMoneyFlow);

    }


}
