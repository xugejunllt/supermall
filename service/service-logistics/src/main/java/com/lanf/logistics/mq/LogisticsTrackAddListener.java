package com.lanf.logistics.mq;

import com.lanf.common.utils.LogInfo;
import com.lanf.logistics.service.ILogisticsTrackService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.bo.ExpressPushBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.lanf.common.utils.LogFormatUtils.printFormatLog;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.ADD_LOGISTICS_TRACK_TOPIC, consumerGroup = TopicName.ADD_LOGISTICS_TRACK_GROUP)
public class LogisticsTrackAddListener implements RocketMQListener<ExpressPushBO> {


    @Autowired
    private ILogisticsTrackService logisticsTrackService;


    @Override
    public void onMessage(ExpressPushBO expressPushBO) {

        printFormatLog(log, "添加轨迹信息", Arrays.asList(new LogInfo("number",
                expressPushBO.getLastResult().getNu())), expressPushBO);

        logisticsTrackService.LogisticsTrackAdd(expressPushBO);


    }
}