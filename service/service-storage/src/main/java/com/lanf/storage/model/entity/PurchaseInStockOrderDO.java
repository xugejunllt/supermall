package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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



    @ApiModelProperty(value = "单据编码")
    private String code;

    @ApiModelProperty(value = "采购单id")
    private Long purchaseOrderId;

    @ApiModelProperty(value = "预计入库数量")
    private Integer expectStorageQuantity;

    @ApiModelProperty(value = "实际入库数量")
    private Integer actualStorageQuantity;

    @ApiModelProperty(value = "入库状态0:待入库,1:部分入库 2:已入库 ")
    private Integer storageStatus;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    @ApiModelProperty(value = "供应商id")
    private Long supplierId;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @TableField( fill = FieldFill.INSERT)
    private String  tenantCode;
}
