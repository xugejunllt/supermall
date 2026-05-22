package com.lanf.order.mq.listener;

import com.lanf.api.order.mq.message.OrderPaySuccessMessage;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.entity.MainOrderDO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.enums.PayStatusEnum;
import com.lanf.order.service.order.IMainOrderService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OrderPayInfo;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 交易成功事件 - 订单服务消费者
 * 更新订单状态
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = TopicName.TRADE_SUCCESS_EVENT_TOPIC,
        consumerGroup = TopicName.TRADE_SUCCESS_ORDER_GROUP
)
public class TradeSuccessOrderListener implements RocketMQListener<TradeSuccessEventMessage> {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IMainOrderService mainOrderService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(TradeSuccessEventMessage message) {

        log.info("交易单更新成功消息,更新订单状态为已支付开始[{}]", JsonUtils.toJsonString(message));
        Boolean bathPay = message.getBathPay();
        Long userId = message.getUserId();

        if (bathPay) {

            log.info("批量支付订单处理开始");

            Long mainOrderId = message.getMainOrderId();
            MainOrderDO orderDO = mainOrderService.lambdaQuery()
                    .eq(BaseEntity::getId, mainOrderId)
                    .eq(MainOrderDO::getUserId, userId)
                    .one();

            if (orderDO == null) {
                log.error("订单不存在");
                return;
            }
            if (PayStatusEnum.PAID.getCode().equals(orderDO.getPayStatus())) {
                log.warn("订单已支付");
                return;
            }

            List<OrderDO> orderDOList = orderService.lambdaQuery()
                    .eq(OrderDO::getUserId,userId)
                    .eq(OrderDO::getMainOrderId, message.getMainOrderId())
                    .list();
            if (orderDOList.isEmpty()) {
                log.error("订单不存在");
                return;
            }
            for (OrderDO orderDO2 : orderDOList) {
                updateOrderStatusCheck(orderDO2);
            }
            List<OrderStatusTraceDO> statusTraceDOList = new ArrayList<>();
            List<OrderPaySuccessMessage> orderPaySuccessMessageList = new ArrayList<>();
            Date date = new Date();

            for (OrderDO orderDO2 : orderDOList) {
                OrderStatusTraceDO statusTraceDO = new OrderStatusTraceDO();
                statusTraceDO.setOrderId(orderDO2.getId());
                statusTraceDO.setFromStatus(OrderStatusEnum.WAIT_PAY);
                statusTraceDO.setToStatus(OrderStatusEnum.PAID);
                statusTraceDO.setUserId(userId);
                statusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
                statusTraceDO.setRemark("订单支付成功");
                statusTraceDO.setTenantId(orderDO2.getTenantId());
                statusTraceDOList.add(statusTraceDO);
                //
                OrderPaySuccessMessage orderPaySuccessMessage = new OrderPaySuccessMessage();
                orderPaySuccessMessage.setOrderId(orderDO.getId());
                orderPaySuccessMessage.setUserId(userId);
                orderPaySuccessMessageList.add(orderPaySuccessMessage);
            }


            boolean update = mainOrderService.lambdaUpdate()
                    .eq(BaseEntity::getId, mainOrderId)
                    .eq(MainOrderDO::getUserId, userId)
                    .eq(MainOrderDO::getVersion, orderDO.getVersion())
                    .eq(MainOrderDO::getPayStatus, PayStatusEnum.WAIT_PAY.getCode())
                    .set(MainOrderDO::getPayStatus, PayStatusEnum.PAID.getCode())
                    .set(MainOrderDO::getVersion, orderDO.getVersion() + 1)
                    .update();
            if (!update) {
                log.warn("更新主订单状态为已支付失败");
                throw new MessageRetryConsumeException("更新主订单状态为已支付失败");
            }

            for (OrderDO orderDO2 : orderDOList) {
                boolean update2 = orderService.lambdaUpdate()
                        .eq(BaseEntity::getId, orderDO2.getId())
                        .eq(OrderDO::getUserId, userId)
                        .eq(OrderDO::getVersion, orderDO2.getVersion())
                        .set(OrderDO::getStatus, OrderStatusEnum.PAID.getCode())
                        .set(OrderDO::getVersion, orderDO2.getVersion() + 1)
                        .update();
                if (!update2) {
                    log.warn("更新订单状态为已支付失败");
                    throw new MessageRetryConsumeException("更新订单状态为已支付失败");
                }
            }
            orderStatusTraceService.saveBatch(statusTraceDOList);
            orderPaySuccessMessageList.forEach(a -> {

                rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                        OrderStatusEnum.PAID.getTag(),JsonUtils.toJsonString(a),
                        a.getOrderId().toString());
            });
            log.info("批量支付订单处理成功");

        } else {

            log.info("单笔支付订单处理开始");

            Date date = new Date();
            OrderPayInfo orderPayInfo = message.getOrderPayInfoList().get(0);
            OrderDO orderDO = orderService.lambdaQuery()
                    .eq(OrderDO::getId, orderPayInfo.getOrderId())
                    .eq(OrderDO::getUserId, userId)
                    .one();
            updateOrderStatusCheck(orderDO);

            OrderStatusTraceDO statusTraceDO = new OrderStatusTraceDO();
            statusTraceDO.setOrderId(orderDO.getId());
            statusTraceDO.setFromStatus(OrderStatusEnum.WAIT_PAY);
            statusTraceDO.setToStatus(OrderStatusEnum.PAID);
            statusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
            statusTraceDO.setRemark("订单支付成功");
            statusTraceDO.setUserId(orderDO.getUserId());
            statusTraceDO.setTenantId(orderDO.getTenantId());

            OrderPaySuccessMessage orderPaySuccessMessage = new OrderPaySuccessMessage();
            orderPaySuccessMessage.setOrderId(orderDO.getId());
            orderPaySuccessMessage.setUserId(orderDO.getUserId());


            boolean update = orderService.lambdaUpdate()
                    .eq(BaseEntity::getId, orderDO.getId())
                    .eq(OrderDO::getUserId, userId)
                    .eq(OrderDO::getVersion, orderDO.getVersion())
                    .set(OrderDO::getStatus, OrderStatusEnum.PAID.getCode())
                    .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                    .update();
            if (!update) {
                log.warn("订单更新为已支付失败");
                throw new MessageRetryConsumeException("订单更新为已支付失败");
            }
            orderStatusTraceService.save(statusTraceDO);
            rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                    OrderStatusEnum.PAID.getTag(),JsonUtils.toJsonString(orderPaySuccessMessage),
                    orderDO.getId().toString());
            log.info("单笔支付订单处理成功");


        }


    }

    private void updateOrderStatusCheck( OrderDO orderDO) {

        if (orderDO == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
        if (OrderStatusEnum.PAID.equals(orderDO.getStatus())) {
            log.warn("订单已为支付状态");
            throw new BizException("订单已为支付状态");
        }

        if (!OrderStatusEnum.WAIT_PAY.equals(orderDO.getStatus())) {
            log.warn("订单已被其他业务场景更新");
            throw new BizException("订单已被其他业务场景更新");
        }

    }
}
