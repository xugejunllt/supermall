package com.lanf.seckill.service.strategy.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.model.dto.PlaceDTO;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillMqExecuteMessage;
import com.lanf.seckill.service.strategy.SecKillStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
 class MqQueueSecKillStrategy implements SecKillStrategy {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Override
    public void executeSecKill(PlaceDTO dto) {
        /**
         * 秒杀请求发送到mq队列排队
         */
        SecKillMqExecuteMessage message = new SecKillMqExecuteMessage();
        message.setUserId(dto.getUserId());
        message.setSecKillItemId(dto.getSeckillItemId());
        rocketMqClient.sendMessage(SecKillMqTopicName.SEC_KILL_MQ_EXECUTE_TOPIC,
                JsonUtils.toJsonString(message));
    }

    @Override
    public Integer getSupportedMode() {
        return SeckillModeEnum.MQ_QUEUE.getCode();
    }
}
