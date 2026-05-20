package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemPageVO implements Serializable {


    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    private String goodsTitle;

    /**
     * skuId
     */
    private Long skuId;


    /**
     * sku名称
     */
    private String skuName;

    private String skuPictureAddress;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;


}
