package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SalesInStockOrderItemAddDTO implements Serializable {


    @ApiModelProperty(value = "商品名称")
    private String goodsName;


    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单位")
    private String skuName;
    private String skuCode;





}
