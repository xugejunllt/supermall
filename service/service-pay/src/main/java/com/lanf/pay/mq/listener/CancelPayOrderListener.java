package com.lanf.pay.mq.listener;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.RefundEventTypeEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.model.bo.CancelWaitPayOrderBO;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.model.enums.TradeStatusEnum;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.QueryRefundResultMessage;
import com.lanf.pay.service.pay.*;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 交易成功事件 - 支付服务消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = TopicName.CANCEL_PAY_ORDER_TOPIC,
        consumerGroup = TopicName.CANCEL_PAY_ORDER_TOPIC_GROUP
)
public class CancelPayOrderListener implements RocketMQListener<CancelOrderMessage> {

    @Autowired
    private IPaymentCancelRecordService paymentCancelRecordService;
    @Autowired
    private PaymentServiceFactory paymentServiceFactory;

    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IRefundOrderService refundOrderService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(CancelOrderMessage message) {

        log.info("取消支付订单,查询三方支付订单支付状态:{}", JsonUtils.toJsonString(message));

        Integer payType = message.getPayType();
        String outTradeNo = message.getOutTradeNo();
        /**
         * 查询三方交易单状态
         */
        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        /**
         * 把钱包支付方式加入进去
         */
        TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);
        TradeStatusEnum tradeStatus = tradeStatusBO.getTradeStatus();

        switch (tradeStatus) {

            case TRADE_FINISHED:
                log.info("三方支付单交易结束");
                return;
            case UNKNOWN:
                log.info("取消未知状态支付订单");
                CancelWaitPayOrderBO cancelWaitPayOrderBO = new CancelWaitPayOrderBO();
                cancelWaitPayOrderBO.setOutTradeNo(message.getOutTradeNo());
                cancelWaitPayOrderBO.setPayType(message.getPayType());
                cancelWaitPayOrderBO.setCurrentPayStatus(TradeStatusEnum.UNKNOWN.getCode());
                paymentCancelRecordService.cancelWaitPayOrder(cancelWaitPayOrderBO);
                return;
            case TRADE_SUCCESS:

                log.info("取消已支付的订单,发起退款");
                PayOrderFlowDO orderFlowDO = payOrderFlowService.lambdaQuery()
                        .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                        .eq(PayOrderFlowDO::getPayType, payType)
                        .one();

                if (orderFlowDO == null) {
                    log.error("支付流水不存在");
                    throw new MessageRetryConsumeException("支付流水不存在");
                }

                RefundOrderDO orderDO = refundOrderService.lambdaQuery().eq(RefundOrderDO::getOutTradeNo, outTradeNo)
                        .one();
                if (orderDO == null) {
                    log.info("退款单不存在,进行创建");
                    RefundOrderDO refundOrderDO = new RefundOrderDO();
                    refundOrderDO.setOutTradeNo(outTradeNo);
                    refundOrderDO.setReturnMoney(orderFlowDO.getTradeMoney());
                    refundOrderDO.setStatus(RefundStatusEnum.REFUNDING);
                    refundOrderDO.setRefundEventType(RefundEventTypeEnum.CANCEL_PAID_ORDER);
                    refundOrderDO.setPayChannel(PayChannelEnum.getByCode(orderFlowDO.getPayType()));
                    refundOrderDO.setBizOrderId(message.getBizOrderId());
                    refundOrderDO.setRefundReason("取消订单退款");
                    try {
                        refundOrderService.save(refundOrderDO);
                    } catch (DuplicateKeyException e) {
                        log.warn("重复插入,退款单已存在");

                    }
                } else {
                    log.info("退款单已存在");
                }
                log.info("发起三方退款请求");
                paymentService.cancelPaidOrder(outTradeNo, orderFlowDO.getTradeMoney(), "取消订单退款");
                log.info("发起成功");

                log.info("发送退款结果查询消息");
                QueryRefundResultMessage queryRefundResultMessage = new QueryRefundResultMessage();
                queryRefundResultMessage.setOutTradeNo(outTradeNo);
                queryRefundResultMessage.setOutRequestNo(outTradeNo);
                rocketMqClient.sendMessage(PayMqTopicName.QUERY_REFUND_RESULT_TOPIC, JsonUtils.toJsonString(queryRefundResultMessage));
        }

        log.info("查询三方支付订单支付状态成功");


    }
}
