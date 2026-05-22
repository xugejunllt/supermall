package com.lanf.order.mq.listener.event;

import com.lanf.api.order.mq.message.OrderShippedMessage;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.order.model.bo.ShippingSubscribeBO;
import com.lanf.order.model.entity.ShippingInfoDO;
import com.lanf.order.model.enums.SubStatusEnum;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.shipping.IShippingInfoService;
import com.lanf.order.service.shipping.Kuaidi100Service;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener( topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = OrderMqGroupName.SHIPPED_ORDER_EVENT_PUSH_SHIPPING_INFO,
        selectorExpression = OrderTopicWithTag.TAG_SHIPPED)
public class ShippedOrderEventPushShippingInfoListener implements RocketMQListener<OrderShippedMessage> {


    @Autowired
    private IShippingInfoService shippingInfoService;
    @Autowired
    private Kuaidi100Service kuaidi100Service;

    @Override
    public void onMessage(OrderShippedMessage orderShippedMessage) {

        log.info("监听到订单发货消息,推送物流信息到快递100:{}", orderShippedMessage);
        Long orderId = orderShippedMessage.getOrderId();

        ShippingInfoDO one = shippingInfoService.lambdaQuery()
                .eq(ShippingInfoDO::getOrderId, orderShippedMessage.getOrderId())
                .eq(ShippingInfoDO::getUserId, orderShippedMessage.getUserId())
                .one();
        if (one == null){
            log.error("订单物流信息不存在");
            return;
        }
        if (SubStatusEnum.SUBSCRIBED.equals(one.getSubStatus())){
            log.warn("物流信息已订阅");
            return;
        }

        ShippingSubscribeBO expressSubscribeBO = new ShippingSubscribeBO();
        expressSubscribeBO.setLogisticsCode(one.getLogisticsCode());
        expressSubscribeBO.setTrackingNumber(one.getTrackingNumber());
        kuaidi100Service.subscribe(expressSubscribeBO);

        boolean update = shippingInfoService.lambdaUpdate()
                .eq(ShippingInfoDO::getOrderId, orderId)
                .eq(ShippingInfoDO::getUserId, orderShippedMessage.getUserId())
                .eq(ShippingInfoDO::getVersion, one.getVersion())
                .set(ShippingInfoDO::getSubStatus, SubStatusEnum.SUBSCRIBED)
                .set(ShippingInfoDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.error("更新物流信息为已订阅状态失败");
            throw new MessageRetryConsumeException("更新物流信息为已订阅状态失败");
        }


    }
}
