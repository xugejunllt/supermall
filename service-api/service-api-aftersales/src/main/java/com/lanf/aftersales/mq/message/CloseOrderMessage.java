package com.lanf.aftersales.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CloseOrderMessage implements Serializable {

    private Long orderId;
    private Long userId;

}
