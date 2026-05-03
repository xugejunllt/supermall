package com.lanf.pay.model.bo;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.PayOrderFlowStatusEnum;
import com.lanf.pay.model.enums.PayOrderTradeStatusEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ReconciliationTradeInfo implements Serializable {

    private String outTradeNo;

    /**
     * 交易金额
     */
    private BigDecimal tradeMoney;
    /**
     * 实际收入金额
     */
    private BigDecimal receiptMoney;

    private PayChannelEnum payChannel;
    /**
     * 0:交易成功 1：交易失败
     */
    private PayOrderFlowStatusEnum status;
    /**
     * 三方支付订单状态
     */
    private  PayOrderTradeStatusEnum tradeStatus;
    private ReconciliationBusinessTypeEnum reconciliationBusinessType;

}
