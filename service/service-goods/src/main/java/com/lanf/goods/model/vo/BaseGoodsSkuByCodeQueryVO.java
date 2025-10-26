package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseGoodsSkuByCodeQueryVO implements Serializable {

    private Long goodsId;

    @ApiModelProperty(value = "skuCode")
    private String skuCode;
    //可使用库存
    private Integer usableStock;
    @ApiModelProperty(value = "sku名称")
    private String skuName;
    @ApiModelProperty(value = "sku名称")
    private String attribute;

    @ApiModelProperty(value = "sku描述")

    private String attributeDesc;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    private List<SkuNameVO> skuNameVOList;

    @ApiModelProperty(value = "排序（影响展示顺序）")
    private Integer sort;
}
