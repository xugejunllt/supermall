package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Data
public class BaseGoodsSkuAddDTO implements Serializable {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "sku名称")
    private String attribute;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "sku描述")
    private String attributeDesc;

    @ApiModelProperty(value = "排序（影响展示顺序）")
    private Integer sort;




}
