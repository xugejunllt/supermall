package com.lanf.order.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeliveryDTO implements Serializable {

    private Long orderId;
    //快递公司id
    private Long expressId;

    /**
     * 快递单号
     */
    private String number;



}
