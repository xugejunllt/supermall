package com.lanf.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.constant.utils.UserContext;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.OrderDetailCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单事件监听器 - 更新订单详情缓存
 * 监听 ORDER_EVENT_TOPIC 所有订单状态变更事件，更新订单详情缓存
 *
 * @author lanf
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = OrderMqGroupName.ORDER_EVENT_UPDATE_DETAIL_CACHE_GROUP,
        consumeMode = ConsumeMode.ORDERLY
)
public class OrderEventUpdateDetailCacheListener implements RocketMQListener<MessageExt> {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private OrderDetailCacheService orderDetailCacheService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String tags = messageExt.getTags();
        String body = new String(messageExt.getBody());
        log.info("收到订单事件消息, tag={}, body={}", tags, body);

        // 解析消息体获取 orderId
        Map<String, Object> messageMap = JSON.parseObject(body, Map.class);
        Object orderIdObj = messageMap.get("orderId");
        if (orderIdObj == null) {
            log.warn("订单事件消息中无orderId, tag={}, body={}", tags, body);
            return;
        }
        Long orderId = Long.valueOf(orderIdObj.toString());

        // 根据 orderId 查询订单获取 userId
        OrderDO orderDO = orderService.getById(orderId);
        if (orderDO == null) {
            log.warn("订单不存在, orderId={}", orderId);
            return;
        }

        // 设置 UserContext
        UserContext.setUserId(orderDO.getUserId());
        try {
            OrderDetailQuery query = new OrderDetailQuery();
            query.setOrderId(orderId);
            OrderDetailForAdminVO detail = orderService.loadOrderDetailFromDB(query);
            if (detail != null) {
                orderDetailCacheService.setOrderDetailToCache(orderId, detail);
                log.info("订单详情缓存更新成功, orderId={}", orderId);
            }
        } finally {
            UserContext.clear();
        }

    }
}
