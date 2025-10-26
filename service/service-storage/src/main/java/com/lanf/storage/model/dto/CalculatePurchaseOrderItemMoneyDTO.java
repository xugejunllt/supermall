package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CalculatePurchaseOrderItemMoneyDTO implements Serializable {

    @NotNull( message = "sku编码不能为空")
    //sku编码,库存最小单位
    private String skuCode;

    @Max(value = 1000000,message = "数量用超过最大值")
    @Min(value = 1,message = "数量小于最小值")
    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @Max(value = 1000000,message = "销售单价超过最大值")
    @Min(value = 1,message = "销售单价小于最小值")
    @ApiModelProperty(value = "销售单价")
    private BigDecimal salesUnitPrice;


}
