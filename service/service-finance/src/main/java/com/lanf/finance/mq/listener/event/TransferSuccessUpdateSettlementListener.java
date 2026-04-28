package com.lanf.finance.mq.listener.event;

import com.lanf.client.pay.model.enums.TransferEventTypeEnum;
import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.constant.TransferEventTagConstant;
import com.lanf.client.pay.mq.message.TransferSuccessMessage;
import com.lanf.finance.model.entity.ClearingOrderDO;
import com.lanf.finance.model.enums.ClearingOrderStatusEnum;
import com.lanf.finance.mq.constant.FinanceMqGroupName;
import com.lanf.finance.service.IClearingOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
    topic =  PayClientTopicName.TRANSFER_SUCCESS_EVENT_TOPIC,
    consumerGroup = FinanceMqGroupName.TRANSFER_SUCCESS_UPDATE_SETTLEMENT_GROUP,
    selectorExpression = TransferEventTagConstant.ORDER_SETTLEMENT
)
public class TransferSuccessUpdateSettlementListener implements RocketMQListener<TransferSuccessMessage> {

    @Autowired
    private IClearingOrderService liquidationService;

    @Override
    public void onMessage(TransferSuccessMessage message) {
        log.info("收到订单结算成功消息: {}", message);

        if (!TransferEventTypeEnum.ORDER_SETTLEMENT.equals(message.getEventType())) {
            log.warn("事件类型不匹配，期望: ORDER_SETTLEMENT, 实际: {}", message.getEventType());
            return;
        }

        Long bizOrderId = message.getBizOrderId();
        try {
            boolean updated = liquidationService.lambdaUpdate()
                    .eq(ClearingOrderDO::getOrderId, bizOrderId)
                    .eq(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.WAIT_SETTLEMENT)
                    .set(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.SETTLED)
                    .update();

            if (updated) {
                log.info("订单 {} 结算状态更新为已结算", bizOrderId);
            } else {
                log.warn("订单 {} 结算状态更新失败，可能已被处理", bizOrderId);
            }

        } catch (Exception e) {
            log.error("处理订单结算成功消息异常，订单ID: {}", bizOrderId, e);
            throw new RuntimeException("处理订单结算消息异常", e);
        }
    }
}
