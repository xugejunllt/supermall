package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CompensatePaymentOrderMessage implements Serializable {

    private String outTradeNo;

    /**
     * 支付类型
     */
    private Integer payType;
    /**
     * 重试次数（第几次重试）
     *
     */
    private Integer retryLevel;

    private Boolean bathOrder;
}
