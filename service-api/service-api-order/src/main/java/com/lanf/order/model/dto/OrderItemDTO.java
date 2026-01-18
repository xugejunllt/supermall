package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemDTO implements Serializable {


    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    private String goodsTitle;

    @ApiModelProperty(value = "skuId")
    private Long skuId;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    private String skuPictureAddress;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单价")
    private BigDecimal unitPrice;
    //商品版本
    private Long goodsVersion;
    //sku 版本
    private Long skuVersion;

}
