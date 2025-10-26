package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GoodsSkuDetailVO implements Serializable {

    @ApiModelProperty(value = "skuCode")
    private String skuCode;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "成本价格")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "商品库存")
    private Integer stock;




}
