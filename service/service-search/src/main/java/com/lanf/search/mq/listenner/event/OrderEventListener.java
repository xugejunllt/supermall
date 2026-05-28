package com.lanf.search.mq.listenner.event;

import com.lanf.api.order.api.OrderApiService;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.vo.OrderDocumentVO;
import com.lanf.api.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.search.model.document.OrderDocument;
import com.lanf.search.mq.constant.SearchMqGroupName;
import com.lanf.search.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单创建成功 同步订单索引到ES
 * 监听所有订单事件
 */

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = SearchMqGroupName.ORDER_CREATE_SUCCESS_EVENT_ADD_ORDER_INDEX_GROUP,
        topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumeMode = ConsumeMode.ORDERLY
)
public class OrderEventListener implements RocketMQListener<MessageExt> {

    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void onMessage(MessageExt message) {

        log.info("监听到订单状态变更事件,同步订单信息到ES");
        String tags = message.getTags();
        OrderDocument orderDocument = null;
        String messageBody = new String(message.getBody());
        log.info("消息tag是:{}", tags);
        try {
            if (OrderTopicWithTag.TAG_WAIT_PAY.equals(tags)) {
                log.info("订单创建事件");
                OrderCreateSuccessMessage message1 = JsonUtils.toObject(messageBody,
                        OrderCreateSuccessMessage.class);
                OrderDocumentQuery query = new OrderDocumentQuery();
                query.setOrderId(message1.getOrderId());
                query.setUserId(message1.getUserId());
                OrderDocumentVO orderDocumentVO = RpcResultParser.parseResult(orderApiService.orderDocumentQuery(query));
                orderDocument = getOrderDocument(orderDocumentVO);
            } else {
                Map object = JsonUtils.toObject(messageBody,
                        Map.class);
                OrderStatusEnum anEnum = OrderStatusEnum.getByTag(tags);
                Long orderId = (Long) object.get("orderId");
                log.info("订单id:{}", orderId);
                orderDocument = orderRepository.findById(orderId).get();
                orderDocument.setOrderStatus(anEnum.getCode());
                orderDocument.setVersion(orderDocument.getVersion()+1);
            }
            log.info("更新订单索引");
            //存入 ES（默认覆盖模式，确保数据最新）
            orderRepository.save(orderDocument);

        } catch (Exception e) {
            log.error("订单创建成功，同步订单索引到ES失", e);
        }


    }


    private static OrderDocument getOrderDocument(OrderDocumentVO orderDocumentVO) {
        OrderDocument orderDocument = new OrderDocument();
        orderDocument.setOrderId(orderDocumentVO.getOrderId());
        orderDocument.setUserId(orderDocumentVO.getUserId());
        orderDocument.setOrderNumber(orderDocumentVO.getOrderNumber());
        orderDocument.setTenantId(orderDocumentVO.getTenantId());
        orderDocument.setOrderStatus(orderDocumentVO.getOrderStatus().getCode());
        orderDocument.setCreateTime(orderDocumentVO.getCreateTime().getTime());
        orderDocument.setGoodsName(orderDocumentVO.getGoodsNames());
        orderDocument.setVersion(1L);
        return orderDocument;
    }


}