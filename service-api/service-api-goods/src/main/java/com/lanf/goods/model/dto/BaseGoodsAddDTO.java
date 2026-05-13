package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class BaseGoodsAddDTO implements Serializable {

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "图片地址，多个,用“，”隔开")
    private String pictureAddress;

    private List<List<BaseGoodsSkuAddDTO>> baseGoodsSkuAddList;

}
