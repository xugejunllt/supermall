package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("purchase_order_item")
public class PurchaseOrderItemDO extends BaseEntity {

private static final long serialVersionUID=1L;


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

    /**采购单价 */
    private BigDecimal buyUnitPrice;

    //total_money
    /** 总金额 */
    private BigDecimal totalMoney;


    /** 备注 */
    private String remarks;
    private Long  tenantId;


}
