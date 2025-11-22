package com.lanf.goods.model.entity;

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
 * @since 2024-06-09
 */
@Data
@TableName("base_goods_sku")
public class BaseGoodsSkuDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private Long goodsId;

    @ApiModelProperty(value = "skuCode")
    private String skuCode;

    @ApiModelProperty(value = "sku名称")
    private String attribute;

    @ApiModelProperty(value = "sku描述")

    private String attributeDesc;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "排序（影响展示顺序）")
    private Integer sort;
    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;



}
