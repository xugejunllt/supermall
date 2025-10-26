package com.lanf.finance.mq;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.service.ILiquidationService;
import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LiquidationDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.LIQUIDATION_TOPIC, consumerGroup = TopicName.LIQUIDATION_GROUP)
public class LiquidationListener implements RocketMQListener<LiquidationDTO> {

    @Autowired
    private ILiquidationService liquidationService;

    @ConsumeMessage
    @Override
    public void onMessage(LiquidationDTO message) {

        log.info("监听清算事件:{}", message);

        liquidationService.createLiquidation(message);


    }
}