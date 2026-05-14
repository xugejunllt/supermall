package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 采购入库单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("purchase_in_stock_order")
public class PurchaseInStockOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;



    /** 单据编码 */
    private String code;

    /** 采购单id */
    private Long purchaseOrderId;

    /** 预计入库数量 */
    private Integer expectStorageQuantity;

    /** 实际入库数量 */
    private Integer actualStorageQuantity;

    /** 入库状态0:待入库,1:部分入库 2:已入库 */
    private Integer storageStatus;

    /** 备注 */
    private String remarks;

    private Long  tenantId;
}
