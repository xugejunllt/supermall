package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * API商品SKU VO
 */
@Data
public class ApiGoodsSkuVO implements Serializable {
    
    /** skuId */
    private Long id;
    
    private String goodsName;

    private String goodsTitle;

    private Long goodsId;

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
    
    private Integer upDownStatus;
    
    private Long shopId;

}
