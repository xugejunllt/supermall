package com.lanf.logistics.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LogisticsAddDTO implements Serializable {

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    private Long userId;

    @ApiModelProperty(value = "快递公司id")
    private Long expressId;

    @ApiModelProperty(value = "快递单号")
    private String number;

    @ApiModelProperty(value = "发货地址id")
    private Long useDeliveryAddressId;

    @ApiModelProperty(value = "收货地址")
    private String toAddress;
    private Long businessId;

}
