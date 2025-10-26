package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDTO implements Serializable {

    private Long id;

    private Long shopId;

    @ApiModelProperty(value = "商家id")
    private Long businessId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "收货地址,json串")
    private String takeAddress;

    private List<OrderItemDTO> orderItemDTOList;
}
