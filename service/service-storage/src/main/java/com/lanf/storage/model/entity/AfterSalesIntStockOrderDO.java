package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 售后入库单
 * </p>
 *
 * @author jarven
 * @since 2026-04-24
 */
@TableName("after_sales_int_stock_order")
@Data
public class AfterSalesIntStockOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;



    /** 单据编码 */
    private String code;

    /** 售后单id */
    private Long afterSalesOrderId;

    /** 预计入库数量 */
    private Integer expectQuantity;

    /** 实际入库数量 */
    private Integer actualQuantity;

    /** 出库状态0:待入库, 1:已入库 */
    private Integer storageStatus;

    /** 仓库id */
    private Long warehouseId;

    /** 备注 */
    private String remarks;

    private Long version;
    private Long  tenantId;


}
