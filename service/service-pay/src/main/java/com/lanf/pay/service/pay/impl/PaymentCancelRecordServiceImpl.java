package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.PaymentCancelRecordMapper;
import com.lanf.pay.model.bo.CancelPaidOrderBO;
import com.lanf.pay.model.bo.CancelPaidOrderResultBO;
import com.lanf.pay.model.bo.CancelWaitPayOrderBO;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.PaymentCancelRecordDO;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.service.pay.*;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * <p>
 * 三方支付订单取消记录 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-19
 */
@Slf4j
@Service
public class PaymentCancelRecordServiceImpl extends ServiceImpl<PaymentCancelRecordMapper, PaymentCancelRecordDO> implements IPaymentCancelRecordService {

    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IRefundOrderService refundOrderService;


    @Override
    public void cancelWaitPayOrder(CancelWaitPayOrderBO cancelWaitPayOrderBO) throws MessageRetryConsumeException {


        String outTradeNo = cancelWaitPayOrderBO.getOutTradeNo();
        Integer payType = cancelWaitPayOrderBO.getPayType();
        PaymentCancelRecordDO recordDO = this.lambdaQuery()
                .eq(PaymentCancelRecordDO::getOutTradeNo, outTradeNo)
                .eq(PaymentCancelRecordDO::getPayType, payType).one();

        if ( recordDO != null){
            log.warn("该支付订单已取消");
            return;
        }

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);

        boolean cancelled = paymentService.cancelPendingOrder(outTradeNo);
        if (cancelled) {
            log.info("取消支付订单成功");
            PaymentCancelRecordDO recordDO2 = new PaymentCancelRecordDO();
            recordDO2.setOutTradeNo(outTradeNo);
            recordDO2.setPayType(payType);
            recordDO2.setCancelSource(cancelWaitPayOrderBO.getCancelSource());
            recordDO2.setCurrentPayStatus(cancelWaitPayOrderBO.getCurrentPayStatus());
            try {
                this.save(recordDO2);
            } catch (DuplicateKeyException e) {
                log.warn("该支付订单已取消");
                return;
            }
        } else {
            log.warn("取消支付订单失败");
        }
    }

    @Transactional
    @Override
    public void cancelPaidOrder(CancelPaidOrderBO cancelPaidOrderBO) throws MessageRetryConsumeException {

        String outTradeNo = cancelPaidOrderBO.getOutTradeNo();
        Integer payType = cancelPaidOrderBO.getPayType();
        PaymentCancelRecordDO recordDO = this.lambdaQuery()
                .eq(PaymentCancelRecordDO::getOutTradeNo, outTradeNo)
                .eq(PaymentCancelRecordDO::getPayType, payType).one();

        if ( recordDO != null){
            log.warn("三方支付订单已取消");
            return;
        }
        PayOrderFlowDO orderFlowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo).one();
        if (orderFlowDO == null){
            log.error("支付订单不存在");
            return;
        }
        String outRequestNo = cancelPaidOrderBO.getOutRequestNo();
        RefundOrderDO orderDO = refundOrderService.lambdaQuery()
                .eq(RefundOrderDO::getOutRequestNo, outTradeNo)
                .eq(RefundOrderDO::getOutRequestNo, outRequestNo).one();
        if ( orderDO != null){
            log.info("该退款单已存在");
            return;
        }


        BigDecimal tradeMoney = orderFlowDO.getTradeMoney();
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);
        CancelPaidOrderResultBO cancelled = paymentService.cancelPaidOrder(outTradeNo,
                tradeMoney, "取消订单");

        if (cancelled != null && cancelled.getResult()){
            log.info("取消三方支付订单,退款成功");
            PaymentCancelRecordDO recordDO2 = new PaymentCancelRecordDO();
            recordDO2.setOutTradeNo(outTradeNo);
            recordDO2.setPayType(payType);
            recordDO2.setCancelSource(cancelPaidOrderBO.getCancelSource());
            recordDO2.setCurrentPayStatus(cancelPaidOrderBO.getCurrentPayStatus());
            //
            RefundOrderDO refundOrderDO = new RefundOrderDO();
            refundOrderDO.setOutTradeNo(outTradeNo);
            refundOrderDO.setOutRequestNo(outRequestNo);
            refundOrderDO.setTradeNo(cancelled.getTradeNo());
            refundOrderDO.setReturnMoney(cancelled.getReturnMoney());
            refundOrderDO.setBuyerLogonId(cancelled.getBuyerLogonId());
            refundOrderDO.setPayOrderId(orderFlowDO.getId());
            refundOrderDO.setPartialRefund(0);
            refundOrderDO.setPayType(payType);
            refundOrderDO.setRefundReason("取消订单");
            try {
                this.save(recordDO2);
                refundOrderService.save(refundOrderDO);

            } catch (DuplicateKeyException e) {
                log.warn("该支付订单已取消");
            }


        }else {
            log.warn("取消三方支付订单,退款失败");
        }


    }
}
