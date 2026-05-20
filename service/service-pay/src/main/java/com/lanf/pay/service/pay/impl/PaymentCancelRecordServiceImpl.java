package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.enums.RefundEventTypeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.ProcessRefundMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.PaymentCancelRecordMapper;
import com.lanf.pay.model.bo.CancelPaidOrderBO;
import com.lanf.pay.model.bo.CancelWaitPayOrderBO;
import com.lanf.pay.model.entity.PaymentCancelRecordDO;
import com.lanf.pay.service.pay.*;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private PaymentServiceFactory paymentServiceFactory;
    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IRefundOrderService refundOrderService;
    @Autowired
    private RocketMqClient rocketMqClient;

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

        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);

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
            log.warn("支付订单已取消");
            return;
        }
        PaymentCancelRecordDO recordDO2 = new PaymentCancelRecordDO();
        recordDO2.setOutTradeNo(outTradeNo);
        recordDO2.setPayType(payType);
        recordDO2.setCancelSource(cancelPaidOrderBO.getCancelSource());
        recordDO2.setCurrentPayStatus(cancelPaidOrderBO.getCurrentPayStatus());
        //
        ProcessRefundMessage message = new ProcessRefundMessage();
        message.setOutTradeNo(outTradeNo);
        message.setOutRequestNo(cancelPaidOrderBO.getOutRequestNo());
        message.setBizOrderId(null);
        message.setRefundEventTypeEnum(RefundEventTypeEnum.CANCEL_PAID_ORDER);
        message.setPayType(payType);
        try {
            this.save(recordDO2);
        } catch (DuplicateKeyException e) {
            log.info("该支付订单已取消");
            return;
        }
        rocketMqClient.sendMessage(PayClientTopicName.PROCESS_REFUND_TOPIC,
                JsonUtils.toJsonString(message));
    }
}
