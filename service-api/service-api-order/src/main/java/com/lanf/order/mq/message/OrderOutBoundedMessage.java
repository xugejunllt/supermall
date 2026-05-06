package com.lanf.order.mq.message;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class OrderOutBoundedMessage implements Serializable {

    private Long orderId;

}
