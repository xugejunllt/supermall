package com.lanf.seckill.mq.listener;

import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.message.SecKillOrderCreatedMessage;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.model.entity.SecKillOrderDO;
import com.lanf.seckill.model.enums.SecKillOrderStatusEnum;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.service.ISecKillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderClientTopicName.SEC_KILL_ORDER_CREATED_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_STATUS_UPDATE_TOPIC
)
public class SecKillOrderCreatedListener implements RocketMQListener<SecKillOrderCreatedMessage> {


    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private ISecKillOrderService secKillOrderService;


    @Transactional
    @Override
    public void onMessage(SecKillOrderCreatedMessage message) {

        SecKillOrderDO oned = secKillOrderService.lambdaQuery()
                .eq(SecKillOrderDO::getOrderNumber, message.getOrderNumber())
                .one();
        if (SecKillOrderStatusEnum.CREATING.equals(oned.getOrderStatus())
                || SecKillOrderStatusEnum.CREATE_FAILED.equals(oned.getOrderStatus())) {
            log.warn("秒杀单已更新");
            return;
        }
        SecKillOrderStatusEnum orderStatus = null;
        if (message.getResult()) {
            orderStatus = SecKillOrderStatusEnum.CREATED;
        } else {
            orderStatus = SecKillOrderStatusEnum.CREATE_FAILED;
        }

        boolean update = secKillOrderService.lambdaUpdate()
                .eq(BaseEntity::getId, oned.getId())
                .eq(SecKillOrderDO::getVersion, oned.getVersion())
                .set(SecKillOrderDO::getOrderStatus, orderStatus)
                .set(SecKillOrderDO::getVersion, oned.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新秒杀单失败");
            throw new MessageRetryConsumeException("更新秒杀单失败");
        }

    }


}
