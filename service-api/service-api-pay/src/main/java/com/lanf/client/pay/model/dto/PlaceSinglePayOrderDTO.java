package com.lanf.client.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PlaceSinglePayOrderDTO implements Serializable {


    @ApiModelProperty(value = "订单编号")
    private String orderNumber;

    @ApiModelProperty(value = "用户id")
    private Long userId;


    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "下单时间")
    private Date placeOrderTime;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeMoney;

    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;










}
