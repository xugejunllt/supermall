package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelWaitPayOrderMessage implements Serializable {

    private String  outTradeNo;

    private Integer payType;

    private Integer cancelSource;

    /**
     * 三方支付订单当前状态 0:未发起交易,1:待支付 ,2: 已支付，进行退款
     */
    private Integer currentPayStatus;
}
