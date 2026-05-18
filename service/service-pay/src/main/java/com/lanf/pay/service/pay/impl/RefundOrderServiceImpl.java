package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.RefundEventTypeEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.RefundOrderMapper;
import com.lanf.pay.model.bo.CancelPaidOrderResultBO;
import com.lanf.pay.model.bo.ProcessRefund;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.RefundQueryResultProcessorMessage;
import com.lanf.pay.service.pay.*;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 退款单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-27
 */
@Slf4j
@Service
public class RefundOrderServiceImpl extends ServiceImpl<RefundOrderMapper, RefundOrderDO> implements IRefundOrderService {


    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IRefundOrderFlowService refundOrderFlowService;

    @Override
    public void processRefund(ProcessRefund processRefund) {


        String outTradeNo = processRefund.getOutRequestNo();
        Integer payType = processRefund.getPayType();
        PayOrderFlowDO orderFlowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType)
                .one();
        if (orderFlowDO == null){
            log.error("支付订单不存在");
            return;
        }
        /**
         * 只支持全额退款
         */
        BigDecimal receiptMoney = orderFlowDO.getReceiptMoney();
        RefundOrderDO orderDO = this.lambdaQuery()
                .eq(RefundOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (orderDO == null){

            RefundOrderDO refundOrderDO = new RefundOrderDO();
            refundOrderDO.setOutTradeNo(outTradeNo);
            refundOrderDO.setReturnMoney(receiptMoney);
            refundOrderDO.setStatus( RefundStatusEnum.REFUNDING);
            refundOrderDO.setRefundEventType(RefundEventTypeEnum.CANCEL_PAID_ORDER);
            refundOrderDO.setPayChannel(PayChannelEnum.getByCode(orderFlowDO.getPayType()));
            refundOrderDO.setBizOrderId(processRefund.getBizOrderId());
            //暂时写死
            refundOrderDO.setRefundReason("取消订单");
            try {
                this.save(refundOrderDO);
            } catch (DuplicateKeyException e) {
                log.warn("退款单已存在");

            }

        }

        /**
         * 只保证请求发送成功 不作业务处理
         */
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);
        CancelPaidOrderResultBO resultBO = paymentService.cancelPaidOrder(outTradeNo, receiptMoney, "取消订单");

        RefundQueryResultProcessorMessage queryRefundResultProcessorMessage =
                new RefundQueryResultProcessorMessage();
        queryRefundResultProcessorMessage.setOutTradeNo(outTradeNo);
        queryRefundResultProcessorMessage.setOutRequestNo(outTradeNo);
        queryRefundResultProcessorMessage.setStatus(RefundFlowStatusEnum.FAILED);
        queryRefundResultProcessorMessage.setPayOrderId(orderFlowDO.getId());
        queryRefundResultProcessorMessage.setPayChannelEnum(PayChannelEnum.getByCode(payType));
        queryRefundResultProcessorMessage.setFailReason(resultBO.getErrorMsg());
        queryRefundResultProcessorMessage.setUpdateStatusRefundStatus(RefundStatusEnum.FAILED);
        rocketMqClient.sendMessage(PayMqTopicName.QUERY_REFUND_RESULT_TOPIC, JsonUtils.toJsonString(queryRefundResultProcessorMessage));

    }





}
