package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OnePlaceAnOrderDTO implements Serializable {


    private Long skuId;

    private Integer quantity;

    private Long couponId;

    private  String orderNumber;

    //收货地址
    private String takeAddress;
}
