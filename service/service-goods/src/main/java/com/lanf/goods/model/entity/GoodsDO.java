package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 基础商品
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Data
@TableName("goods")
public class GoodsDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "商品编码")
    private String code;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;
    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "副标题")
    private String title;

    @ApiModelProperty(value = "图片地址")
    private String pictureAddress;

    @ApiModelProperty(value = "商品3级分类")
    private Long categoryId;

    @ApiModelProperty(value = "品牌")
    private Long brandId;

    @ApiModelProperty(value = "上下架状态 0:上架 ,1:下架")
    private Integer upDownStatus;
    //搜索提示词标签
    private String promptWordLabel;
    //扩展标签 用于展示
    private String extendedTags;
    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;

    private Long version;


}
