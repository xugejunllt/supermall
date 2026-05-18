package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemVO implements Serializable {

    /**
     * 商品名称
     */
    private String goodsName;
    private String goodsTitle;
    /**
     * sku编码,库存最小单位
     */
    private String skuCode;
    private Long goodsId;
    /**
     * 总数量
     */
    private Integer quantity;

    /**
     * 单位
     */
    private String unit;
    /**
     * sku名称
     */
    private String skuName;

    private String skuPictureAddress;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 总金额
     */
    private BigDecimal totalMoney;


}
