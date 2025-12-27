package com.lanf.welfare.mq;

import com.lanf.messagemanager.client.annotation.ConsumeMessage;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.DeductCouponTemplateCountMsg;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.DEDUCT_COUPON_TEMPLATE_COUNT_TOPIC, consumerGroup = TopicName.DEDUCT_COUPON_TEMPLATE_COUNT_GROUP,maxReconsumeTimes = TopicName.MAX_RECONSUME_TIMES)
public class DeductCouponTemplateCountListener implements RocketMQListener<DeductCouponTemplateCountMsg> {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @Override
    @ConsumeMessage
    public void onMessage(DeductCouponTemplateCountMsg message) {

        log.info("消费消息:message{}:",message);
        couponTemplateService.deductCouponTemplateCount( message);

    }

}