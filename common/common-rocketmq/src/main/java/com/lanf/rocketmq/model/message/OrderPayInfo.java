package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderPayInfo implements Serializable {

    private Long orderId;
    /**
     * 支付金额
     */
    private BigDecimal payMoney;

    private Integer payType;

    //商家id
    private Long merchantId;
}
