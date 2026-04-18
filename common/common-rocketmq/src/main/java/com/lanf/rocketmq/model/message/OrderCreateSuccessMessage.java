package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderCreateSuccessMessage implements Serializable {

    private String outTradeNo;

    /**
     * 是否 批次订单
     */
    private Boolean bathPay;

    private Integer payType;
}
