package com.lanf.finance.mq;

import com.lanf.client.pay.mq.PayClientTopicName;
import com.lanf.client.pay.mq.message.PayOrderFlowInsertSuccessMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.finance.constant.FinanceMqGroupName;
import com.lanf.finance.model.bo.AddMoneyFlow;
import com.lanf.finance.service.IMoneyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 插入资金流水
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC,
        consumerGroup = FinanceMqGroupName.PAY_ORDER_FLOW_FINANCE_GROUP
)
public class PayOrderFlowInsertSuccessFinanceListener implements RocketMQListener<PayOrderFlowInsertSuccessMessage> {
    @Autowired
    private IMoneyFlowService moneyFlowService;

    @Override
    public void onMessage(PayOrderFlowInsertSuccessMessage message) {

        log.info("插入支付流水成功:[{}]", JsonUtils.toJsonString(message));

        AddMoneyFlow addMoneyFlow = new AddMoneyFlow();
        addMoneyFlow.setBusinessId(Constants.PLATFORM_BUSINESS_ID);
        addMoneyFlow.setBizOrderId(message.getBizOrderId());
        addMoneyFlow.setRecordType(message.getRecordType());
        addMoneyFlow.setIncomeMoney(message.getReceiptMoney());
        moneyFlowService.addMoneyFlow(addMoneyFlow);


    }



}
