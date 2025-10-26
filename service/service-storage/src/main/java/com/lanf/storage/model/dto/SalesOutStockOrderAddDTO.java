package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SalesOutStockOrderAddDTO implements Serializable {

    @ApiModelProperty(value = "订单id")
    private Long orderId;








}
