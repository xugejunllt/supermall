package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品项BO
 */
@Data
public class GoodsItemBO implements Serializable {

    private Long skuId;

    private Long cartId;

    private String skuName;

    private String goodsName;
    
    /** 数量 */
    private Integer quantity;
    
    /** 价格 */
    private BigDecimal price;
    
    private String skuCode;

    private Long goodsId;
    
    private String goodsTitle;
    
    private String skuPictureAddress;
    
    /** 商品版本 */
    private Long goodsVersion;
    
    /** sku 版本 */
    private Long skuVersion;
    
    private Long warehouseId;

}
