package com.lanf.finance.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.service.IMoneyFlowService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.MoneyFlowDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.MONEY_FLOW_TOPIC, consumerGroup = TopicName.MONEY_FLOW_GROUP)
public class MoneyFlowListener implements RocketMQListener<String> {

    @Autowired
    private IMoneyFlowService moneyFlowService;

    @Override
    public void onMessage(String message) {

        log.info("监听资金流水添加事件:{}", message);

        try {

            MoneyFlowDTO object = JsonUtils.toObject(message, MoneyFlowDTO.class);

            moneyFlowService.moneyFlowAdd(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}