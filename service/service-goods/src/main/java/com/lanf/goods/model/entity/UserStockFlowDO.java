package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 库存流水
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Data
@TableName("user_stock_flow")
public class UserStockFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "0:采购入库")
    private Integer orderType;

    @ApiModelProperty(value = "商品sku编码")
    private String skuCode;

    @ApiModelProperty(value = "关联的出入库单id")
    private String bizNumber;

    @ApiModelProperty(value = "出库数量")
    private Integer outQuantity;

    @ApiModelProperty(value = "入库数量")
    private Integer inQuantity;

    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    //同步时间,用于与仓储库存对账
    private Date syncTime;




}
