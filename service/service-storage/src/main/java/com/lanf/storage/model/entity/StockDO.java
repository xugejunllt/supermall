package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Data
@TableName("stock")
public class StockDO extends BaseEntity {

private static final long serialVersionUID=1L;

    //`sku_code`, `warehouse_id` 唯一索引 用于并发控制

    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    private String goodsName;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    //单位
    private String unit;

    /**
     * 可使用库存
     */
    private Integer usableStock;

    @ApiModelProperty(value = "锁住的库存")
    private Integer lockStock;



    //版本号 乐观锁控制
    private Long version;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;


    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;

}
