package com.lanf.storage.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PurchaseInStockOrderItemDetailVO implements Serializable {

    private Long id;
    /** 商品名称 */
    private String goodsName;

    /** sku编码,库存最小单位 */
    private String skuCode;

    /** 总数量 */
    private Integer totalQuantity;

    /** 剩余数量 */
    private Integer surplusQuantity;

    /** 实际入库数量 */
    private Integer actualQuantity;

    /** 单位 */
    private String unit;


    
}
