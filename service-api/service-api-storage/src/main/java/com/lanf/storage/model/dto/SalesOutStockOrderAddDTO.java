package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SalesOutStockOrderAddDTO implements Serializable {

    private Integer expectOutQuantity;
    @ApiModelProperty(value = "物流公司")
    private String expressCompany;

    @ApiModelProperty(value = "快递单号")
    private String expressNumber;

    private List<InOutStockOrderItemDTO> inOutStockOrderItemDTOList;


}
