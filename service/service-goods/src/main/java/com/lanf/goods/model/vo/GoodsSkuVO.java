package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GoodsSkuVO implements Serializable {

    private Long id;
    @ApiModelProperty(value = "skuCode")
    private String skuCode;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "商品库存")
    private Integer stock;
    private Long businessId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;
    private Long goodsId;
    @ApiModelProperty(value = "数量")
    private Integer quantity;

}
