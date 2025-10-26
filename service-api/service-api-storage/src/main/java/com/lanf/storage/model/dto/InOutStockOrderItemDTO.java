package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class InOutStockOrderItemDTO implements Serializable {

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;

    @ApiModelProperty(value = "总数量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "单位")
    private String unit;
}
