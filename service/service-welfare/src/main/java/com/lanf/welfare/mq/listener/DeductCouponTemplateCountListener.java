package com.lanf.welfare.mq.listener;

import com.lanf.rocketmq.model.TopicName;
import com.lanf.welfare.mq.constant.WelfareMqGroupName;
import com.lanf.welfare.mq.constant.WelfareMqTopicName;
import com.lanf.welfare.mq.message.DeductCouponTemplateCountMessage;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(topic = WelfareMqTopicName.DEDUCT_COUPON_TEMPLATE_COUNT_TOPIC,
        consumerGroup = WelfareMqGroupName.DEDUCT_COUPON_TEMPLATE_COUNT_GROUP,
        maxReconsumeTimes = TopicName.MAX_RECONSUME_TIMES)
public class DeductCouponTemplateCountListener implements RocketMQListener<DeductCouponTemplateCountMessage> {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @Override
    public void onMessage(DeductCouponTemplateCountMessage message) {

        log.info("消费消息:message{}:",message);
        couponTemplateService.deductCouponTemplateCount( message);

    }

}