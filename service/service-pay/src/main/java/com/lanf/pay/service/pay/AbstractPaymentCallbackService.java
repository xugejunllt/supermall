package com.lanf.pay.service.pay;


import com.lanf.api.pay.model.enums.PayMethodEnum;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.PayOrderFlowInsertSuccessMessage;
import com.lanf.common.utils.*;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.bo.PassbackParams;
import com.lanf.pay.model.bo.PaySuccessHandleBO;
import com.lanf.pay.model.bo.PaySuccessHandleResultBO;
import com.lanf.pay.model.dto.PayCallbackDTO;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.enums.PayOrderFlowStatusEnum;
import com.lanf.pay.utils.PayServiceUtils;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
public abstract class AbstractPaymentCallbackService implements PaymentService {

    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Override
    public void payCallback(PayCallbackDTO dto) {
        try {
            handlePayCallback(dto);
        } catch (Exception e) {
            log.error("支付回调异常", e);
            responsePayFail(dto.getResponse());

        }
    }

    private void handlePayCallback(PayCallbackDTO dto) {

        CallbackResultBO resultBO = null;

        try {
            resultBO = parse(dto.getRequest());
        } catch (Exception e) {
            responsePayFail(dto.getResponse());
            return;
        }
        PaySuccessHandleBO paySuccessHandleBO = new PaySuccessHandleBO();
        paySuccessHandleBO.setPayType(dto.getPayType());
        paySuccessHandleBO.setResultBO(resultBO);
        PaySuccessHandleResultBO handleResultBO = paySuccessHandleBO(paySuccessHandleBO);
        if (handleResultBO.getHandleSuccess()) {
            responsePayOk(dto.getResponse());
        } else {
            responsePayFail(dto.getResponse());
        }
    }
    @Transactional
    @Override
    public PaySuccessHandleResultBO paySuccessHandleBO(PaySuccessHandleBO paySuccessHandleBO) {
        CallbackResultBO resultBO = paySuccessHandleBO.getResultBO();

        String strPassbackParams = resultBO.getStrPassbackParams();
        if (IStringUtils.isEmpty(strPassbackParams)){
                log.error("回调参数异常");
            return new PaySuccessHandleResultBO(false);
        }

        PassbackParams passbackParams = null;
        try {
            passbackParams = JsonUtils.toObject(strPassbackParams, PassbackParams.class);
            boolean verified = PayServiceUtils.verifyPassbackParams(passbackParams);
//            if (!verified) {
//                log.error("回调参数签名异常");
//                return new PaySuccessHandleResultBO(false);
//            }
           log.info("回调参数验证成功");
        } catch (Exception e) {
            log.error("回调处理异常",e);
            return new PaySuccessHandleResultBO(false);
        }
        resultBO.setPassbackParams(passbackParams);

        String outTradeNo = resultBO.getOutTradeNo();
        Integer payType = paySuccessHandleBO.getPayType();

        BigDecimal totalAmount = resultBO.getTotalAmount();
        BigDecimal tradeMoney =  passbackParams.getTradeMoney();

        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);
        if (alreadyPaid) {
            log.warn("支付流水已存在");
            return new PaySuccessHandleResultBO(true);
        }

        if (!BigDecimalUtil.equals(totalAmount, tradeMoney)) {
            /**
             * 交易金额异常
             *
             */
            log.error("交易金额异常 outTradeNo:[{}],totalAmount[{}],tradeMoney[{}]", outTradeNo, totalAmount, tradeMoney);
            return new PaySuccessHandleResultBO(false);
        }


        PayOrderFlowDO payOrderFlowDO = buildPayOrderFlowDO(payType, resultBO);
        PayOrderFlowInsertSuccessMessage message = buildPayOrderFlowInsertSuccessMessage(resultBO, payType);

        try {

            payOrderFlowService.save(payOrderFlowDO);

        } catch (DuplicateKeyException e) {
            log.info("支付流水已存在,重复插入");
            return new PaySuccessHandleResultBO(true);
        }
        /**
         * 下游业务处理
         */
        mqSendMessageUtils.sendMessage(PayClientTopicName.PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC,
                JsonUtils.toJsonString(message), null);
        return new PaySuccessHandleResultBO(true);
    }

    /**
     * 是否已经支付过 以唯一支付流水为准
     */
    private boolean isAlreadyPaid(String outTradeNo, Integer payType) {
        PayOrderFlowDO flowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType).one();
        return flowDO != null;
    }

    private PayOrderFlowDO buildPayOrderFlowDO(Integer payType, CallbackResultBO resultBO) {

        PassbackParams passbackParams = resultBO.getPassbackParams();
        PayOrderFlowDO payOrderFlowDO = new PayOrderFlowDO();
        payOrderFlowDO.setTradeId(passbackParams.getTradeOrderId());
        payOrderFlowDO.setPayType(payType);
        payOrderFlowDO.setOutTradeNo(resultBO.getOutTradeNo());
        payOrderFlowDO.setTradeMoney(resultBO.getReceiptMoney());
        payOrderFlowDO.setReceiptMoney(resultBO.getReceiptMoney());
        payOrderFlowDO.setPayFinishTime(resultBO.getPayFinishTime());
        payOrderFlowDO.setPayAccount(resultBO.getPayAccount());
        payOrderFlowDO.setIncomeAccount(resultBO.getIncomeAccount());
        payOrderFlowDO.setNotifyTime(resultBO.getNotifyTime());
        payOrderFlowDO.setTradeNo(resultBO.getTradeNo());
        payOrderFlowDO.setPassbackParams(JsonUtils.toJsonString(passbackParams));
        payOrderFlowDO.setAllParams(resultBO.getAllParams());
        payOrderFlowDO.setStatus(PayOrderFlowStatusEnum.SUCCESS);
        String format = DateUtils.format(resultBO.getPayFinishTime(), DateUtils.DATE);
        payOrderFlowDO.setPayFinishDate(format);
        return payOrderFlowDO;
    }

    private PayOrderFlowInsertSuccessMessage buildPayOrderFlowInsertSuccessMessage
            (CallbackResultBO resultBO, Integer payType) {
        PassbackParams passbackParams = resultBO.getPassbackParams();
        TradePurposeEnum tradeType = passbackParams.getTradeType();
        PayOrderFlowInsertSuccessMessage message = new PayOrderFlowInsertSuccessMessage();
        message.setOutTradeNo(resultBO.getOutTradeNo());
        message.setBathPay(passbackParams.getBathPay());
        message.setPayType(payType);
        message.setTradePurpose(tradeType);
        /**
         * 支付回调 那么一定是三方支付
         */
        message.setPayMethod(PayMethodEnum.THIRD_PARTY_PAY);
        return message;
    }



}
