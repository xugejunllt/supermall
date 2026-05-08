package com.lanf.order.mq.listener;

import com.lanf.constant.exception.BizException;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.enums.OrderProcessStepEnum;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.mq.message.SecKillOrderConfirmMessage;
import com.lanf.order.service.IOrderService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.SEC_KILL_ORDER_CONFIRM_TOPIC,
        consumerGroup = OrderMqGroupName.SEC_KILL_ORDER_CONFIRM_GROUP)
public class SecKillOrderConfirmListener implements RocketMQListener<SecKillOrderConfirmMessage> {

    @Autowired
    private IOrderService orderService;

    @Transactional
    @Override
    public void onMessage(SecKillOrderConfirmMessage message) {

        String orderNumber = message.getOrderNumber();
        OrderDO one = orderService.lambdaQuery()
                .eq(OrderDO::getOrderNumber, orderNumber)
                .one();
        if (one == null) {
            log.error("订单不存在:{}", orderNumber);
            throw new BizException("订单不存在");
        }
        OrderStatusEnum status = one.getStatus();
        if ( !OrderStatusEnum.WAIT_CONFIRM.equals( status)){
            log.warn("订单已确认");
            return;
        }
        OrderProcessStepEnum orderProcessStep = message.getOrderProcessStep();
        if (OrderProcessStepEnum.STOCK_DEDUCT_FAILED.equals(orderProcessStep)) { 
            log.info("库存扣减失败,取消订单");
            z
            return;
        }
        
        String orderProcessSteps = one.getOrderProcessSteps();
        Set<Integer> stepSet = Arrays.stream(orderProcessSteps.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        stepSet.add(orderProcessStep.getCode());

        if (stepSet.containsAll(OrderProcessStepEnum.getConfirmSuccessSet())) {
            log.info("订单确认成功，所有步骤已完成: orderNumber={}", orderNumber);
            z
        } else {

            log.info("订单确认中，等待其他步骤完成: orderNumber={}", orderNumber);
            String updatedSteps = stepSet.stream()
                    .map(String::valueOf)  // 将 Integer 转换为 String
                    .sorted()               // 排序（保证顺序一致，如 "0,1,2"）
                    .collect(Collectors.joining(",")); // 用逗号连接
            boolean update = orderService.lambdaUpdate()
                    .eq(BaseEntity::getId, one.getId())
                    .eq(OrderDO::getVersion, one.getVersion())
                    .set(OrderDO::getOrderProcessSteps, updatedSteps)
                    .set(OrderDO::getVersion, one.getVersion() + 1)
                    .update();
            if (!update) {
                log.warn("订单状态更新异常");
                throw new MessageRetryConsumeException("订单状态更新异常");
            }


        }

    }
}
