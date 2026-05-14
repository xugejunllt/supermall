package com.lanf.search.mq.listenner.event;

import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.api.order.api.OrderApiService;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.vo.OrderDocumentVO;
import com.lanf.api.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.search.model.document.OrderDocument;
import com.lanf.search.mq.constant.SearchMqGroupName;
import com.lanf.search.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单创建成功 同步订单索引到ES
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = SearchMqGroupName.ORDER_CREATE_SUCCESS_EVENT_ADD_ORDER_INDEX_GROUP,
        topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumeMode = ConsumeMode.ORDERLY,
        selectorExpression = OrderTopicWithTag.TAG_WAIT_PAY)
public class OrderCreateSuccessEventListener implements RocketMQListener<OrderCreateSuccessMessage> {

     @Autowired
     private OrderApiService orderApiService;
     @Autowired
     private OrderRepository  orderRepository;

     @Override
     public void onMessage(OrderCreateSuccessMessage message) {

          OrderDocumentQuery query = new OrderDocumentQuery();
          query.setOrderId(message.getOrderId());
          query.setUserId(message.getUserId());

          OrderDocumentVO orderDocumentVO = RpcResultParser.parseResult(orderApiService.orderDocumentQuery(query));
          OrderDocument orderDocument = getOrderDocument(orderDocumentVO);
          //存入 ES（默认覆盖模式，确保数据最新）
          orderRepository.save(orderDocument);



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