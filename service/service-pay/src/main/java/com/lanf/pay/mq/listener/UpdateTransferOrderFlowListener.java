package com.lanf.pay.mq.listener;

/**
 * 插入流水
 */

import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.model.enums.TransferStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.TransferQueryResultProcessorMessage;
import com.lanf.pay.service.pay.ITransferOrderService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.TRANSFER_QUERY_RESULT_TOPIC,
        consumerGroup = PayMqGroupName.UPDATE_TRANSFER_ORDER_GROUP
)
public class UpdateTransferOrderFlowListener implements RocketMQListener<TransferQueryResultProcessorMessage> {


    @Autowired
    private ITransferOrderService transferOrderService;

    @Override
    public void onMessage(TransferQueryResultProcessorMessage message) {


        String outTradeNo = message.getOutTradeNo();
        TransferOrderDO orderDO1 = transferOrderService.lambdaQuery()
                .eq(TransferOrderDO::getOutTradeNo, outTradeNo).one();
        if (orderDO1 == null) {
            log.error("退款单不存在");
            return;
        }
        if (TransferStatusEnum.SUCCESS.equals(orderDO1.getStatus())
                || TransferStatusEnum.FAILED.equals(orderDO1.getStatus())) {
            log.info("退款单已处理");
            return;
        }

        /**
         * 这里异步处理
         */
        boolean update = transferOrderService.lambdaUpdate()
                .eq(TransferOrderDO::getId, orderDO1.getId())
                .eq(TransferOrderDO::getVersion, orderDO1.getVersion())
                .eq(TransferOrderDO::getStatus, RefundStatusEnum.REFUNDING)
                .set(TransferOrderDO::getStatus, message.getUpdateTransferStatus())
                .set(TransferOrderDO::getVersion, orderDO1.getVersion() + 1)
                .update();

        if (!update) {
            log.warn("更新退款单失败");
            throw new MessageRetryConsumeException("更新退款单失败");
        }


    }
}
