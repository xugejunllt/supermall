package com.lanf.seckill.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SecKillSuccessMessage implements Serializable {

    private Long userId;

    private Long secKillItemId;

    private String orderNumber;

    private Integer itemQuantity;

}
