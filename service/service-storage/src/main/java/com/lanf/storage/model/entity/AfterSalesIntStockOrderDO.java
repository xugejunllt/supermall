package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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



    @ApiModelProperty(value = "单据编码")
    private String code;

    @ApiModelProperty(value = "售后单id")
    private Long afterSalesOrderId;

    @ApiModelProperty(value = "预计入库数量")
    private Integer expectQuantity;

    @ApiModelProperty(value = "实际入库数量")
    private Integer actualQuantity;

    @ApiModelProperty(value = "出库状态0:待入库, 1:已入库 ")
    private Integer storageStatus;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    @ApiModelProperty(value = "备注")
    private String remarks;

    private Long version;


}
