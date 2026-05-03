package com.lanf.pay.model.bo;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.ReconciliationTradeStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ReconciliationTradeInfo implements Serializable {


    private PayChannelEnum payChannel;

    private String outTradeNo;
    /**
     * 实际收入金额
     */
    private BigDecimal receiptMoney;

    private ReconciliationTradeStatusEnum reconciliationTradeStatus;
    /**
     * 三方账单支付完成时间
     */
    private String payFinishTime;


}
