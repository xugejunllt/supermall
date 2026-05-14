package com.lanf.api.order.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SecKillOrderCreatedMessage implements Serializable {


    private String orderNumber;
    /**
     * 确认结果
     */
    private Boolean result;
}
