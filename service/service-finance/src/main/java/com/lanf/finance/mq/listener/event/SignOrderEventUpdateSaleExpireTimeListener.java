package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.mq.constant.FinanceMqGroupName;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.finance.service.ILiquidationService;
import com.lanf.order.mq.OrderClientTopicName;
import com.lanf.order.mq.message.SignOrderMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * 订单签收时 更新结算单 的售后过期时间
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.SIGN_ORDER_EVENT_TOPIC, consumerGroup = FinanceMqGroupName.SIGN_ORDER_EVENT_FINANCE_GROUP)
public class SignOrderEventUpdateSaleExpireTimeListener implements RocketMQListener<SignOrderMessage> {

    @Autowired
    private ILiquidationService liquidationService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(SignOrderMessage message) {
        log.info("订单签收时开始:[{{}}]", JsonUtils.toJsonString(message));
        Long orderId = message.getOrderId();
        Date signTime = message.getSignTime();
        Integer afterSaleDays = message.getAfterSaleDays();
        Date afterSaleExpireTime = new Date(signTime.getTime() + afterSaleDays * 24 * 60 * 60 * 1000);

        boolean update = liquidationService.lambdaUpdate()
                .eq(LiquidationDO::getOrderId, orderId)
                .set(LiquidationDO::getAfterSaleExpireTime, afterSaleExpireTime)
                .update();
        if (!update){
            log.error("更新结算单失败");
        }

    }


}