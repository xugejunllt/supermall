package com.lanf.pay.mq.listener.event;

import com.lanf.api.order.model.enums.OrderProcessStepEnum;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.SecKillOrderConfirmMessage;
import com.lanf.api.order.mq.message.SecKillPlaneCreateOrderSuccessMessage;
import com.lanf.api.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.constant.PayMqGroupName;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 秒杀 订单创建成功之后 ，创建交易单
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderClientTopicName.SEC_KILL_PLANE_CREATE_ORDER_SUCCESS_EVENT_TOPIC,
        consumerGroup = PayMqGroupName.CREATE_TRADE_ORDER_GROUP
)
public class SecKillPlaneCreateOrderSuccessEventListener implements RocketMQListener<SecKillPlaneCreateOrderSuccessMessage> {

    @Autowired
    private ITradeOrderService tradeOrderService;

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Override
    public void onMessage(SecKillPlaneCreateOrderSuccessMessage message) {


        CreateTradeOrderDTO dto = new CreateTradeOrderDTO();
        dto.setOrderNumber(message.getOrderNumber());
        dto.setUserId(message.getUserId());
        dto.setOrderId(message.getOrderId());
        dto.setTradeMoney(message.getTradeMoney());
        //
        SecKillOrderConfirmMessage message1 = new SecKillOrderConfirmMessage();
        message1.setOrderNumber(message.getOrderNumber());
        message1.setOrderProcessStep(OrderProcessStepEnum.TRADE_CREATED);

        try {
            tradeOrderService.confirmCreateTradeOrder(dto);
        } catch (Exception e) {
            throw new MessageRetryConsumeException("创建交易单失败");
        }
        /**
         * 发送创建成功通知消息
         */
        mqSendMessageUtils.sendMessage(OrderClientTopicName.SEC_KILL_ORDER_CONFIRM_TOPIC,
                JsonUtils.toJsonString(message1),null);

    }
}
