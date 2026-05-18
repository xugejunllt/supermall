package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemPageVO implements Serializable {

    private String goodsTitle;

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
