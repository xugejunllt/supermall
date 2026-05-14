package com.lanf.api.order.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SignOrderMessage implements Serializable {

    private Long orderId;
    /**
     * 签收时间
     */
    private Date signTime;

    /**
     * 售后有效期
     */
    private Integer afterSaleDays;

    /**
     * 支付金额
     */
    private BigDecimal payMoney;

    //商家id
    private Long merchantId;

}
