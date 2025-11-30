package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 店铺信息
 * </p>
 *
 * @author jarven
 * @since 2025-11-30
 */
@Data
@TableName("shop")
public class ShopDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "店铺名称")
    private String name;

    @ApiModelProperty(value = "头像")
    private String headUrl;

    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;




}
