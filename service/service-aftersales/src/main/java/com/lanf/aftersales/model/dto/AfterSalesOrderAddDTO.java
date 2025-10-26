package com.lanf.aftersales.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AfterSalesOrderAddDTO implements Serializable {

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "售后类型 0:退货退款 1:换货")
    private Integer afterSalesType;

    @ApiModelProperty(value = "退款原因")
    private String returnReason;





}
