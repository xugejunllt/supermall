package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class EmptyCartGoodsSkuVO implements Serializable {

    //skuId
    private Long id;

    private Long goodsId;
    private Long shopId;
    //商家id
    private Long businessId;
    private String goodsName;
    private String goodsTitle;
    private String skuCode;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;
    //数量
    private Integer quantity;



}
