package com.lanf.api.pay.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CreateMergeTradeOrderItemDTO implements Serializable {

    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;
    //订单id
    @NotNull( message = "订单id不能为空")
    private Long orderId;

    @NotNull(message = "交易金额不能为空")
    private BigDecimal tradeMoney;




}
