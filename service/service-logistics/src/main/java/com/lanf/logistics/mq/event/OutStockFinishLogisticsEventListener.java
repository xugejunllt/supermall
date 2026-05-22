package com.lanf.logistics.mq.event;


import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OutStockFinishEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.OUT_STOCK_FINISH_EVENT_TOPIC, consumerGroup = TopicName.OUT_STOCK_FINISH_LOGISTICS_EVENT_GROUP)
public class OutStockFinishLogisticsEventListener implements RocketMQListener<OutStockFinishEventMessage> {



    @Override
    public void onMessage(OutStockFinishEventMessage message) {
        log.info("出库完成，添加物流轨迹,发送到批量写入队列中");

    }

}
