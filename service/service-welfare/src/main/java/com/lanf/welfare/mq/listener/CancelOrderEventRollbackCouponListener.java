package com.lanf.welfare.mq.listener;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.OrderCouponDO;
import com.lanf.welfare.model.enums.CouponStatusStatus;
import com.lanf.welfare.service.ICouponService;
import com.lanf.welfare.service.IOrderCouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 回滚商品库存
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = TopicName.CANCEL_ORDER_EVENT_WELFARE_GROUP,
        selectorExpression = OrderTopicWithTag.TAG_CANCELLED

)
public class CancelOrderEventRollbackCouponListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private IOrderCouponService orderCouponService;
    @Autowired
    private ICouponService couponService;

    @Transactional
    @Override
    public void onMessage(CancelOrderEventMessage message) {

        log.info("取消订单事件回滚优惠卷开始:[{{}}]", JsonUtils.toJsonString(message));
        Long orderId = message.getOrderId();

        List<OrderCouponDO> couponDOList = orderCouponService.lambdaQuery()
                .eq(OrderCouponDO::getOrderId, message.getOrderId()).list();
        if (couponDOList.isEmpty()){
            log.info("订单[{}]没有优惠券", orderId);
            return;
        }
        List<Long> couponIdList = couponDOList.stream().map(OrderCouponDO::getCouponId).collect(Collectors.toList());
        List<CouponDO> couponDOList1 = couponService
                .lambdaQuery()
                .eq(CouponDO::getStatus, CouponStatusStatus.USE.getCode())
                .in(CouponDO::getId, couponIdList).list();
        if ( couponDOList1.isEmpty()){
            log.warn("用户优惠卷不存在");
            return;
        }
        for (CouponDO couponDO : couponDOList1) {


            boolean update = couponService.lambdaUpdate()
                    .eq(CouponDO::getId, couponDO.getId())
                    .eq(CouponDO::getVersion, couponDO.getVersion())
                    .set(CouponDO::getStatus, CouponStatusStatus.WAIT.getCode())
                    .set(CouponDO::getVersion, couponDO.getVersion() + 1)
                    .update();
            if ( !update){
                log.warn("优惠卷更新失败");
                throw new MessageRetryConsumeException("优惠卷更新失败");
            }
        }
    }






}