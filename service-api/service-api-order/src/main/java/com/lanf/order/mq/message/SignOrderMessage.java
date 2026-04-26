package com.lanf.order.mq.message;

import lombok.Data;

import java.io.Serializable;
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
}
