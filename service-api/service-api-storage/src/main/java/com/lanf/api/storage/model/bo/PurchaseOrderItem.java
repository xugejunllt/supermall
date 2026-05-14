package com.lanf.api.storage.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItem implements Serializable {

    /** 采购单id */
    private Long purchaseOrderId;

    /** 商品名称 */
    private String goodsName;

    /** sku编码,库存最小单位 */
    private String skuCode;

    /** 数量 */
    private Integer quantity;

    /** 单位 */
    private String unit;

    /** 采购单价 */
    private BigDecimal buyUnitPrice;

    //total_money
    /** 总金额 */
    private BigDecimal totalMoney;

    /** 备注 */
    private String remarks;

}
