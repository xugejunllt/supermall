package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeliveryDTO implements Serializable {

    private Long orderId;
    //快递公司id
    private Long expressId;

    @ApiModelProperty(value = "快递单号")
    private String number;



}
