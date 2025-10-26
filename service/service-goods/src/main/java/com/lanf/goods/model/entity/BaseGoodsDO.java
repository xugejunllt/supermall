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
 * @since 2024-06-09
 */
@Data
@TableName("base_goods")
public class BaseGoodsDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "商品编码")
    private String code;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "图片地址，多个,用“，”隔开")
    private String pictureAddress;


    @TableField( fill = FieldFill.INSERT)
    private String  tenantCode;

}
