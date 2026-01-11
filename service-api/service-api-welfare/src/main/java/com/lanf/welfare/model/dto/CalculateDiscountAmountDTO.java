package com.lanf.welfare.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CalculateDiscountAmountDTO implements Serializable {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    //订单总金额
    @NotNull(message = "订单总金额不能为空")
    private BigDecimal totalAmount;

}
