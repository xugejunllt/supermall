package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class SubmitOrderDTO implements Serializable {

    private String orderNumber;

    @ApiModelProperty(value = "收货地址")
    private String takeAddress;
     //优惠券
    private Set<Long> couponIdSet;
    private List<SubmitOrderItemDTO> submitOrderItemDTOList;

}
