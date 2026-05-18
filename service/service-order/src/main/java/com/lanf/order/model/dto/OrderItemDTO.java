package com.lanf.order.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemDTO implements Serializable {


    /**
     * 订单id
     */
    private Long orderId;

    private Long userId;
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
    private String skuCode;
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
    //商品版本
    private Long goodsVersion;
    //sku 版本
    private Long skuVersion;
    private Long warehouseId;

    private Long tenantId;
}
