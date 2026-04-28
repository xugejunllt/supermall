package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.finance.model.enums.LiquidationStatusEnum;
import com.lanf.finance.service.ILiquidationService;
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
    private ILiquidationService liquidationService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(CancelOrderEventMessage message) {
        log.info("取消订单事件回滚三方支付订单开始:[{{}}]", JsonUtils.toJsonString(message));
        Long orderId = message.getOrderId();
        LiquidationDO liquidationDO = liquidationService.lambdaQuery().eq(LiquidationDO::getOrderId, orderId).one();
        if (liquidationDO == null) {
            /**
             * 结算单创建是发生交易后异步执行的
             * 极端场景下 可能会存在订单取消时结算单不存在
             * 继续重试
             */
            log.warn("结算单不存在");
            throw new MessageRetryConsumeException("结算单不存在");
        }
        if ( !LiquidationStatusEnum.WAIT_SETTLEMENT.equals(liquidationDO.getStatus())){
            log.warn("结算单状态异常");
            return;
        }

        boolean update = liquidationService.lambdaUpdate().eq(LiquidationDO::getId, liquidationDO.getId())
                .eq(LiquidationDO::getStatus, LiquidationStatusEnum.WAIT_SETTLEMENT)
                .eq(LiquidationDO::getVersion, liquidationDO.getVersion())
                .set(LiquidationDO::getStatus, LiquidationStatusEnum.CANCELLED)
                .set(LiquidationDO::getVersion, liquidationDO.getVersion() + 1)
                .update();
        if (!update){
            log.warn("更新结算单失败");
        }
    }


}