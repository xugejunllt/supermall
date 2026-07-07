package com.lanf.pay.mq.listener.event;


import com.lanf.api.pay.model.enums.ClearingStatusEnum;
import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.constant.TransferEventTagConstant;
import com.lanf.api.pay.mq.message.TransferSuccessMessage;
import com.lanf.pay.model.entity.ClearingDetailDO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.clearing.ClearingDetailService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.TRANSFER_SUCCESS_EVENT_TOPIC,
        consumerGroup = PayMqGroupName.TRANSFER_SUCCESS_UPDATE_SETTLEMENT_GROUP,
        selectorExpression = TransferEventTagConstant.ORDER_SETTLEMENT
)
public class TransferSuccessUpdateSettlementListener implements RocketMQListener<TransferSuccessMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;

    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(TransferSuccessMessage message) {
        log.info("收到订单结算成功消息: {}", message);

        if (!TransferEventTypeEnum.ORDER_SETTLEMENT.equals(message.getEventType())) {
            log.error("事件类型不匹配，期望: ORDER_SETTLEMENT, 实际: {}", message.getEventType());
            return;
        }
        ClearingDetailDO clearingDetailDO = clearingDetailService.getById(message.getBizOrderId());
        if (clearingDetailDO == null) {
            log.error("找不到对应的对账单: {}", message.getBizOrderId());
            return;
        }
        if (ClearingStatusEnum.CLEARING_COMPLETED.equals(clearingDetailDO.getStatus())
         || ClearingStatusEnum.EXCEPTION.equals(clearingDetailDO.getStatus())) {
            log.info("对账单已处理完成: {}", clearingDetailDO.getId());
            return;
        }
        if (!ClearingStatusEnum.CLEARING.equals(clearingDetailDO.getStatus())) {
            log.error("结算单状态异常: {}", clearingDetailDO.getId());

            return;
        }

        ClearingStatusEnum status = null;
        if (message.getResult()) {
            status = ClearingStatusEnum.CLEARING_COMPLETED;
        } else {
            status = ClearingStatusEnum.EXCEPTION;
        }

        boolean update = clearingDetailService.lambdaUpdate()
                .eq(ClearingDetailDO::getId, clearingDetailDO.getId())
                .eq(ClearingDetailDO::getStatus, ClearingStatusEnum.CLEARING)
                .eq(ClearingDetailDO::getVersion, clearingDetailDO.getVersion())
                .set(ClearingDetailDO::getStatus, status)
                .set(ClearingDetailDO::getTransferMoney, message.getTransAmount())
                .set(ClearingDetailDO::getVersion, clearingDetailDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新对账单状态失败: {}", clearingDetailDO.getId());

            throw new MessageRetryConsumeException("更新对账单状态失败");
        }


    }
}
