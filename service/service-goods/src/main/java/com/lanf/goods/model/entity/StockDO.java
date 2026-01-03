package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 库存
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Data
@TableName("user_stock")
public class StockDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "可使用库存")
    private Integer usableStock;

    @ApiModelProperty(value = "锁住的库存")
    private Integer lockStock;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @ApiModelProperty(value = "版本号 乐观锁控制")
    private Long version;
    private Long  tenantId;



}
