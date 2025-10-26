package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseGoodsBySkuCodeQueryVO implements Serializable {


    private String skuCode;

    @ApiModelProperty(value = "商品名称")
    private String name;

    private String skuName;

}
