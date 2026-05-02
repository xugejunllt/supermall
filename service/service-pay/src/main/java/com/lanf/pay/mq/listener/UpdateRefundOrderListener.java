package com.lanf.pay.mq.listener;

/**
 * 更新退款单
 */

import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.RefundQueryResultProcessorMessage;
import com.lanf.pay.service.pay.IRefundOrderService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.REFUND_QUERY_RESULT_TOPIC,
        consumerGroup = PayMqGroupName.UPDATE_REFUND_ORDER_GROUP
)
public class UpdateRefundOrderListener implements RocketMQListener<RefundQueryResultProcessorMessage> {


    @Autowired
    private IRefundOrderService refundOrderService;

    @Override
    public void onMessage(RefundQueryResultProcessorMessage message) {


        String outTradeNo = message.getOutTradeNo();
        RefundOrderDO orderDO1 = refundOrderService.lambdaQuery().eq(RefundOrderDO::getOutTradeNo, outTradeNo).one();
        if (orderDO1 == null) {
            log.error("退款单不存在");
            return;
        }
        if (RefundStatusEnum.SUCCESS.equals(orderDO1.getStatus())
                || RefundStatusEnum.FAILED.equals(orderDO1.getStatus())) {
            log.info("退款单已处理");
            return;
        }

        /**
         * 这里异步处理
         */
        boolean update = refundOrderService.lambdaUpdate()
                .eq(RefundOrderDO::getId, orderDO1.getId())
                .eq(RefundOrderDO::getVersion, orderDO1.getVersion())
                .eq(RefundOrderDO::getStatus, RefundStatusEnum.REFUNDING)
                .set(RefundOrderDO::getStatus, message.getUpdateStatusRefundStatus())
                .set(RefundOrderDO::getVersion, orderDO1.getVersion() + 1)
                .update();

        if (!update) {
            log.warn("更新退款单失败");
            throw new MessageRetryConsumeException("更新退款单失败");
        }

    }
}
