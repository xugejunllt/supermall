package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemDTO implements Serializable {


    //skuId
    private Long id;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;
    private String goodsTitle;
    @ApiModelProperty(value = "skuId")
    private Long skuId;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    private String skuCode;

    @ApiModelProperty(value = "单价")
    private BigDecimal unitPrice;
    private BigDecimal price;
    private String skuPictureAddress;


}
