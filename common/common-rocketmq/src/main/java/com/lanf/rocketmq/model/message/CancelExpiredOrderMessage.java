package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelExpiredOrderMessage implements Serializable {

    private Long orderId;

    private Integer cancelSource;

}
