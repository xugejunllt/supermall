package com.lanf.client.pay.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PayOrderFlowInsertSuccessMessage implements Serializable {


    private Long orderId;
    private String outTradeNo;

    /**
     * 是否 批次订单
     */
    private Boolean bathPay;

    private Integer payType;

    /**
     * 实收金额
     */
    private BigDecimal receiptMoney;
}
