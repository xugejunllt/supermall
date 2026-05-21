package com.lanf.api.pay.mq.message;

import com.lanf.api.pay.model.enums.PayMethodEnum;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayOrderFlowInsertSuccessMessage extends BaseMessage {

    private String outTradeNo;
    /**
     * 是否 批次订单
     */
    private Boolean bathPay;

    private Integer payType;

    private TradePurposeEnum tradePurpose;

    private PayMethodEnum payMethod;
    /**
     * 用户实际支付的金额
     */
    private BigDecimal payMoney;

}
