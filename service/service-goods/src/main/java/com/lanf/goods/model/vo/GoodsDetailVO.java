package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GoodsDetailVO implements Serializable {


    private Long id;

    @ApiModelProperty(value = "商品编码")
    private String code;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "副标题")
    private String title;

    @ApiModelProperty(value = "图片地址，多个,用“，”隔开")
    private String pictureAddress;

    @ApiModelProperty(value = "商品3级分类")
    private String categoryName;

    @ApiModelProperty(value = "品牌")
    private String brandName;

    @ApiModelProperty(value = "上下架状态 0:上架 ,1:下架")
    private Integer upDownStatus;

    private List<GoodsSkuDetailVO> goodsSkuDetailVOList;


}
