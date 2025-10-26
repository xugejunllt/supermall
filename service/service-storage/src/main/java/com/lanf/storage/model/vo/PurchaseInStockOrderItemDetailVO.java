package com.lanf.storage.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PurchaseInStockOrderItemDetailVO implements Serializable {

    private Long id;
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;

    @ApiModelProperty(value = "总数量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "剩余数量")
    private Integer surplusQuantity;

    @ApiModelProperty(value = "实际入库数量")
    private Integer actualQuantity;

    @ApiModelProperty(value = "单位")
    private String unit;


    
}
