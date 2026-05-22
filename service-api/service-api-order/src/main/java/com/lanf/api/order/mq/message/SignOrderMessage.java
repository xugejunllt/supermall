package com.lanf.api.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SignOrderMessage extends BaseMessage {

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
    private Long tenantId;

}
