package com.lanf.logistics.mq;

import com.lanf.interfacemonitor.client.annotation.CallMqTask;
import com.lanf.logistics.service.ILogisticsTrackService;
import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.lanf.common.utils.LogFormatUtils.printFormatLog;

/**
 * 批量消费 批量写入 数据可能重复 允许重复
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC, consumerGroup = TopicName.BATH_ADD_LOGISTICS_TRACK_GROUP)
public class LogisticsTrackBathAddListener implements RocketMQListener<LogisticsTrackBathAddDTO> {


    @Autowired
    private ILogisticsTrackService logisticsTrackService;

    /**
     *
     */
    @CallMqTask
    @ConsumeMessage
    @Override
    public void onMessage(LogisticsTrackBathAddDTO logisticsTrackBathAddDTO) {

        log.info("批量写入轨迹信息：{}", logisticsTrackBathAddDTO);

        List<LogisticsTrackBathAddDTO> addDTOList = Arrays.asList(logisticsTrackBathAddDTO);

        logisticsTrackService.logisticsTrackBathAdd(addDTOList);

    }
}