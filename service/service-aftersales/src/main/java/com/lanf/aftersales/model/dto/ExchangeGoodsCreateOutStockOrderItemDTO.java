package com.lanf.aftersales.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExchangeGoodsCreateOutStockOrderItemDTO implements Serializable {

    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

}
