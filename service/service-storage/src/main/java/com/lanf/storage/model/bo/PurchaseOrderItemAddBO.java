package com.lanf.storage.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemAddBO implements Serializable {



    //商品名称
    private String goodsName;

    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "销售单价")
    private BigDecimal salesUnitPrice;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalMoney;
}
