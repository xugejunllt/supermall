package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.client.pay.model.enums.RefundEventTypeEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.exception.BizException;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.FinanceClientTopicName;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.pay.mapper.RefundOrderMapper;
import com.lanf.pay.model.bo.CancelPaidOrderResultBO;
import com.lanf.pay.model.bo.ProcessRefund;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.IRefundOrderService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
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


    @Override
    public void processRefund(ProcessRefund processRefund) {


        String outTradeNo = processRefund.getOutRequestNo();
        String outRequestNo = processRefund.getOutRequestNo();
        Integer payType = processRefund.getPayType();
        PayOrderFlowDO orderFlowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType)
                .one();
        if (orderFlowDO == null){
            log.error("支付订单不存在");
            return;
        }
        RefundOrderDO orderDO = this.lambdaQuery()
                .eq(RefundOrderDO::getOutRequestNo, outTradeNo)
                .eq(RefundOrderDO::getOutRequestNo, outRequestNo).one();
        if ( orderDO != null){
            log.info("该退款单已存在");
            return;
        }
        BigDecimal tradeMoney = orderFlowDO.getTradeMoney();

        /**
         * 这里使用抽象类
         */
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);
        CancelPaidOrderResultBO cancelled = paymentService.cancelPaidOrder(outTradeNo,
                tradeMoney, "取消订单");

        if (cancelled != null && cancelled.getResult()){
            log.info("取消三方支付订单,退款成功");
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
            refundOrderDO.setRefundEventType(processRefund.getRefundEventTypeEnum());
            ///
            AddMoneyFlowMessage moneyFlowMessage = buildAddMoneyFlowMessage(processRefund, cancelled.getReturnMoney());
            try {
                this.save(refundOrderDO);
                /**
                 *
                 * 记录资金流水
                 */
                rocketMqClient.sendMessage(FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC, JsonUtils.toJsonString(moneyFlowMessage));

            } catch (DuplicateKeyException e) {
                log.warn("该支付订单已取消");
            }

        } else {
            log.warn("取消三方支付订单,退款失败");
        }

    }

    private AddMoneyFlowMessage buildAddMoneyFlowMessage(ProcessRefund processRefund,BigDecimal incomeMoney){
        RecordTypeEnum recordTypeEnum = null;

        if (processRefund.getRefundEventTypeEnum()

                .equals(RefundEventTypeEnum.CANCEL_PAID_ORDER)){
            recordTypeEnum = RecordTypeEnum.CANCEL_ORDER_REFUND;

        } else if (processRefund.getRefundEventTypeEnum()
                .equals(RefundEventTypeEnum.AFTER_SALES_REFUND)){
            recordTypeEnum = RecordTypeEnum.AFTER_SALES_REFUND;
        } else {
            log.error("退款事件类型异常");
            throw new BizException("退款事件类型异常");
        }

        AddMoneyFlowMessage moneyFlowMessage = new AddMoneyFlowMessage();
        moneyFlowMessage.setBusinessId(Constants.PLATFORM_BUSINESS_ID);
        moneyFlowMessage.setBizOrderId(processRefund.getBizOrderId());
        moneyFlowMessage.setIncomeMoney(incomeMoney);
        moneyFlowMessage.setRecordType(recordTypeEnum);

        return moneyFlowMessage;
    }
}
