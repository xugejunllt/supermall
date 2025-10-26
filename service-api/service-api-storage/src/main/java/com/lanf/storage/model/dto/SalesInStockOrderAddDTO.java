package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SalesInStockOrderAddDTO implements Serializable {


    @ApiModelProperty(value = "售后单id")
    private Long afterSalesOrderId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "商家id")
    private Long businessId;
    //订单商品总数量
    private Integer totalQuantity;
    private Integer inOutStatus;
    private List<SalesInStockOrderItemAddDTO> salesInStockOrderItemAddDTOList;




}
