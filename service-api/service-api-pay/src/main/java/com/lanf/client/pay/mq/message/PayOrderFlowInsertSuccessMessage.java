package com.lanf.client.pay.mq.message;

import com.lanf.client.pay.model.enums.PayMethodEnum;
import com.lanf.client.pay.model.enums.TradeTypeEnum;
import com.lanf.finance.model.enums.RecordTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PayOrderFlowInsertSuccessMessage implements Serializable {


    private Long bizOrderId;
    private String outTradeNo;

    /**
     * 是否 批次订单
     */
    private Boolean bathPay;

    private Integer payType;
    /**
     * 实际交易金额
     */
    private BigDecimal tradeMoney;
    /**
     * 平台账户实收金额
     */
    private BigDecimal receiptMoney;

    private RecordTypeEnum recordType;

    private TradeTypeEnum tradeType;

    private PayMethodEnum payMethod;
}
