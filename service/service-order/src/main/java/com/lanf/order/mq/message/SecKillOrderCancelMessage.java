package com.lanf.order.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SecKillOrderCancelMessage implements Serializable {

    private Long orderId;
    private String orderNumber;
}
