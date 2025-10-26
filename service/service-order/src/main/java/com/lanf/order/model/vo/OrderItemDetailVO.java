package com.lanf.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderItemDetailVO implements Serializable {


    private String skuName;
    //数量
    private Integer quantity;

    //单价
    private BigDecimal unitPrice;
}
