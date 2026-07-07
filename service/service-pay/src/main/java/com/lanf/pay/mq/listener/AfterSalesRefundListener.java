package com.lanf.pay.mq.listener;

import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.AfterSalesRefundMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.pay.RefundEventTypeEnum;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderMessage;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderClientTopicName.AFTER_SALES_REFUND_TOPIC,
        consumerGroup = PayMqGroupName.AFTER_SALES_REFUND_GROUP
)
public class AfterSalesRefundListener implements RocketMQListener<AfterSalesRefundMessage> {

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Override
    public void onMessage(AfterSalesRefundMessage message) {

        log.info("监听到售后退款消息:{}", message);

        Long orderId = message.getOrderId();

        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery().eq(TradeOrderDO::getOrderId, orderId)
                .one();
        if (tradeOrderDO == null) {
            log.error("交易单不存在:{}", message);
            return;
        }
        if ( !TradeOrderStatusEnum.COMPLETED.equals(TradeOrderStatusEnum.getByCode(tradeOrderDO.getPayStatus()))){

            log.error("交易单为完成支付");
            return;
        }
        CancelOrderMessage message2 = new CancelOrderMessage();
        message2.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        message2.setPayType(tradeOrderDO.getPayType().getCode());
        message2.setBizOrderId(message.getAfterSalesOrderId());
        message2.setRefundEventType(RefundEventTypeEnum.AFTER_SALES_REFUND);

        mqSendMessageUtils.sendMessage(TopicName.CANCEL_PAY_ORDER_TOPIC,
                JsonUtils.toJsonString(message2),null);

    }
}
