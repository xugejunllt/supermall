package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InOutStockOrderItemDTO implements Serializable {

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * sku编码
     */
    private String skuCode;

    /**
     * 总数量
     */
    private Integer totalQuantity;

    /**
     * 单位
     */
    private String unit;
}
