package com.lanf.api.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU BO
 */
@Data
public class GoodsSku implements Serializable {

    private Long skuId;

    private Long goodsId;

    private String shopName;

    private String goodsName;

    private String goodsTitle;

    /** skuCode */
    private String skuCode;

    private String skuName;

    /** sku图片 */
    private String skuPictureAddress;

    /** 价格 */
    private BigDecimal price;

    private Long skuVersion;
    
    private Long goodsVersion;

    private Long warehouseId;

    private Long tenantId;
}
