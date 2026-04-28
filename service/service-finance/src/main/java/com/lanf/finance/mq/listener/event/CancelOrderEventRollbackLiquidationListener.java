package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.model.entity.ClearingOrderDO;
import com.lanf.finance.model.enums.ClearingOrderStatusEnum;
import com.lanf.finance.service.IClearingOrderService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 订单取消时 回滚结算单
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.CANCEL_ORDER_EVENT_TOPIC, consumerGroup = TopicName.CANCEL_ORDER_EVENT_FINANCE_GROUP)
public class CancelOrderEventRollbackLiquidationListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private IClearingOrderService liquidationService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(CancelOrderEventMessage message) {
        log.info("取消订单事件回滚三方支付订单开始:[{{}}]", JsonUtils.toJsonString(message));
        Long orderId = message.getOrderId();
        ClearingOrderDO liquidationDO = liquidationService.lambdaQuery().eq(ClearingOrderDO::getOrderId, orderId).one();
        if (liquidationDO == null) {
            /**
             * 结算单创建是发生交易后异步执行的
             * 极端场景下 可能会存在订单取消时结算单不存在
             * 继续重试
             */
            log.warn("结算单不存在");
            throw new MessageRetryConsumeException("结算单不存在");
        }
        if ( !ClearingOrderStatusEnum.WAIT_SETTLEMENT.equals(liquidationDO.getStatus())){
            log.warn("结算单状态异常");
            return;
        }

        boolean update = liquidationService.lambdaUpdate().eq(ClearingOrderDO::getId, liquidationDO.getId())
                .eq(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.WAIT_SETTLEMENT)
                .eq(ClearingOrderDO::getVersion, liquidationDO.getVersion())
                .set(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.CANCELLED)
                .set(ClearingOrderDO::getVersion, liquidationDO.getVersion() + 1)
                .update();
        if (!update){
            log.warn("更新结算单失败");
        }
    }


}