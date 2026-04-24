package com.lanf.finance.mq.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SalesInStockOrderItemAdd implements Serializable {


    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单位")
    private String skuName;
    private String skuCode;





}
