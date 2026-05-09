package com.lanf.search.mq.listenner.event;

import com.lanf.constant.exception.BizException;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.mq.message.OrderPaySuccessMessage;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.search.model.document.OrderDocument;
import com.lanf.search.mq.constant.SearchMqGroupName;
import com.lanf.search.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import static com.lanf.order.mq.constant.OrderClientTopicName.ORDER_EVENT_TOPIC;
import static com.lanf.order.mq.constant.OrderClientTopicName.TAG_PAID;

/**
 * 订单创建成功 同步订单索引到ES
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = SearchMqGroupName.ORDER_CREATE_SUCCESS_EVENT_ADD_ORDER_INDEX_GROUP,
        topic = ORDER_EVENT_TOPIC,
        consumeMode = ConsumeMode.ORDERLY,
        selectorExpression = TAG_PAID)
public class OrderPaySuccessEventListener implements RocketMQListener<OrderPaySuccessMessage> {


     @Autowired
     private OrderRepository  orderRepository;

     @Override
     public void onMessage(OrderPaySuccessMessage message) {

          // 1. 查询现有订单（包含当前 version）
          OrderDocument order = orderRepository.findById(message.getOrderId())
                  .orElseThrow(() -> new BizException("订单不存在"));

          // 2. 修改状态
          order.setOrderStatus(OrderStatusEnum.PAID.getCode());

          // 3. 保存（此时 Spring Data ES 会自动校验 version）
          //    如果 version 与 ES 中的当前版本不一致，抛出 OptimisticLockingFailureException

          try {
               orderRepository.save(order);
          } catch (OptimisticLockingFailureException e) {
               // 乐观锁冲突，可以重试或通知用户
               log.warn("Update failed due to concurrent modification, " );
               throw new MessageRetryConsumeException("更新订单索引失败");
          }

     }





}