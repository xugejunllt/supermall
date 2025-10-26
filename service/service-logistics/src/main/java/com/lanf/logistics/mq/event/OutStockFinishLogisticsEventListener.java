package com.lanf.logistics.mq.event;


import com.lanf.logistics.service.ILogisticsTrackService;
import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.messagemanager.client.annotation.SendMessage;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.model.message.OutStockFinishEventMessage;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.OUT_STOCK_FINISH_EVENT_TOPIC, consumerGroup = TopicName.OUT_STOCK_FINISH_LOGISTICS_EVENT_GROUP)
public class OutStockFinishLogisticsEventListener implements RocketMQListener<OutStockFinishEventMessage> {

    @Autowired
    private ISendMqMessageService sendMqMessageService;

    @SendMessage
    @ConsumeMessage
    @Override
    public void onMessage(OutStockFinishEventMessage message) {
        log.info("出库完成，添加物流轨迹,发送到批量写入队列中");
        //发送批量消费队列中
        LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = message.getLogisticsTrackBathAddDTO();
        logisticsTrackBathAddDTO.setBizKeyValue(message.getBizKeyValue());
        sendMqMessageService.sendMessage(TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC,logisticsTrackBathAddDTO);
    }

}
