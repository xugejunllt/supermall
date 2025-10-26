package com.lanf.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TradeStatusVO implements Serializable {

    //是否发起交易
    private Boolean exist;
    //0:交易创建，等待买家付款,1:交易支付成功,2:交易结束，不能退款 3:未付款交易超时关闭，或支付完成后全额退款
    private Integer tradeStatus;
    //支付金额

    private BigDecimal totalAmount;
    //实收金额
    private BigDecimal receiptAmount;
}
