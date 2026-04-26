package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 交易成功事件消息
 */
@Data
public class TradeSuccessEventMessage implements Serializable {

    private Boolean bathPay;

    private Long orderId;

    private Long mainOrderId;
    /**
     * 支付金额
     */
    private BigDecimal payMoney;

    private Integer payType;

    //商家id
    private Long merchantId;
}
