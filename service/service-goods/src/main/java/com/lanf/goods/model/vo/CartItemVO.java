package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项VO
 */
@Data
public class CartItemVO implements Serializable {


    private Long cartId;

    private Long goodsId;

    private Long skuId;

    private String skuCode;

    private String skuName;

    private String goodsName;
    
    /** 数量 */
    private Integer quantity;
    
    /** 价格 */
    private BigDecimal price;
    private String skuPictureAddress;
}
