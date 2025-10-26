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


    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "锁住的库存")
    private Integer lockStock;

    @ApiModelProperty(value = "可用库存")
    private Integer usableStock;

    @TableField( fill = FieldFill.INSERT)
    private String  tenantCode;


}
