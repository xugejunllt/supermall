package com.lanf.goods.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GoodsSkuBO implements Serializable {

    private Long skuId;

    private Long goodsId;

    private String goodsName;

    private String goodsTitle;

    @ApiModelProperty(value = "skuCode")
    private String skuCode;

    private String skuName;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    private Long skuVersion;
    private Long goodsVersion;
}
