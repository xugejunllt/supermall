package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 库存流水
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("stock_flow")
public class StockFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long stockId;

    /**
     * 0:销售单出库 1:销售单换货入库 2:销售退货退款入库 3:销售单换货出库 4.采购入库
     */
    private Integer orderType;

    @ApiModelProperty(value = "商品sku编码")
    private String skuCode;


    @ApiModelProperty(value = "出入库单id")
    private String bizNumber;

    @ApiModelProperty(value = "出库数量")
    private Integer outQuantity;

    @ApiModelProperty(value = "入库数量")
    private Integer inQuantity;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;


    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;
}
