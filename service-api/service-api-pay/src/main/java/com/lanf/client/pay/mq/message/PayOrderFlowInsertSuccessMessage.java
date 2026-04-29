package com.lanf.client.pay.mq.message;

import com.lanf.client.pay.model.enums.PayMethodEnum;
import com.lanf.client.pay.model.enums.TradePurposeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PayOrderFlowInsertSuccessMessage implements Serializable {

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
