package com.lanf.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.constant.mq.OrderTopicWithTag;
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
 * 监听 ORDER_EVENT_TOPIC 所有订单状态变更事件（支付、发货、签收、取消等），
 * 异步刷新订单详情缓存，实现缓存与数据库的最终一致性。
 * 核心流程：
 * 1.消费订单状态变更MQ消息
 * 2.解析消息获取orderId和userId
 * 3.设置UserContext上下文
 * 4.调用loadOrderDetailFromDB加载最新订单详情
 * 5.将最新数据写入Redis缓存
 * 设计亮点：
 * - 采用ConsumeMode.ORDERLY顺序消费，避免同一订单并发刷新导致缓存数据错乱
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

        //1.解析消息体获取orderId和userId
        Map<String, Object> messageMap = JSON.parseObject(body, Map.class);
        Object orderIdObj = messageMap.get("orderId");
        Object userIdObj = messageMap.get("userId");

        //2.校验消息中必须包含orderId和userId，缺失时直接抛异常触发MQ重试
        if (orderIdObj == null || userIdObj == null) {
            log.error("订单事件消息中无orderId或userId, tag={}, body={}", tags, body);
//            throw new BizException("订单事件消息中无orderId或userId");
            return;
        }
        Long orderId = Long.valueOf(orderIdObj.toString());
        Long userId = Long.valueOf(userIdObj.toString());


        //4.构建查询条件，加载订单最新详情
        OrderDetailQuery query = new OrderDetailQuery();
        query.setOrderId(orderId);
        query.setUserId(userId);
        OrderDetailForAdminVO detail = orderService.loadOrderDetailFromDB(query);
        log.info("订单详情查询结果, detail={}, ", detail);
        //5.将最新订单详情写入Redis缓存，过期时间7天
        if (detail != null) {
            orderDetailCacheService.setOrderDetailToCache(orderId, detail);
            log.info("订单详情缓存更新成功, orderId={}", orderId);
        }


    }
}
