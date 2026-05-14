package com.lanf.api.order.mq.message;

import lombok.Data;

import java.io.Serializable;


@Data
public class OrderPaySuccessMessage implements Serializable {



    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;


}
