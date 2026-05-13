package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU详情VO
 */
@Data
public class GoodsSkuDetailVO implements Serializable {

    /** skuCode */
    private String skuCode;

    /** sku名称 */
    private String skuName;

    /** sku图片 */
    private String skuPictureAddress;

    /** 价格 */
    private BigDecimal price;

    /** 成本价格 */
    private BigDecimal costPrice;

    /** 商品库存 */
    private Integer stock;

}
