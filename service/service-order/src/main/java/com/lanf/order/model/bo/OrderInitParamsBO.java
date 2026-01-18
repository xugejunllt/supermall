package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderInitParamsBO implements Serializable {

    private Long tradeOrderId;

    private String bizKeyPrx;

    private Long orderId;

    private Long userId;
}
