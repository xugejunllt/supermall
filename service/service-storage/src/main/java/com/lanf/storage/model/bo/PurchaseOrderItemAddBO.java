package com.lanf.storage.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemAddBO implements Serializable {



    //商品名称
    private String goodsName;

    /** sku编码,库存最小单位 */
    private String skuCode;

    /** 数量 */
    private Integer quantity;

    /** 单位 */
    private String unit;

    /** 销售单价 */
    private BigDecimal salesUnitPrice;

    /** 备注 */
    private String remarks;

    /** 总金额 */
    private BigDecimal totalMoney;
}
