package com.lanf.pay.mq;

import com.lanf.pay.service.impl.PayServiceAdapter;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.CANCEL_ORDER_TOPIC, consumerGroup = TopicName.CANCEL_ORDER_GROUP)
public class CancelOrderListener implements RocketMQListener<CancelOrderDTO> {


    @Autowired
    private PayServiceAdapter payServiceAdapter;

    /**
     * 取消订单
     */
    @Override
    public void onMessage(CancelOrderDTO dto) {
        log.info("取消订单:{}", dto);


            payServiceAdapter.cancelTradeOrder(dto.getOrderId());


    }
}