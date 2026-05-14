package com.lanf.api.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemVO implements Serializable {

    @ApiModelProperty(value = "商品名称")
    private String goodsName;
    private String goodsTitle;
    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;
    private Long goodsId;
    @ApiModelProperty(value = "总数量")
    private Integer quantity;

    @ApiModelProperty(value = "单位")
    private String unit;
    @ApiModelProperty(value = "sku名称")
    private String skuName;

    private String skuPictureAddress;

    @ApiModelProperty(value = "单价")
    private BigDecimal unitPrice;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalMoney;


}
