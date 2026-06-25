package com.lanf.welfare.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UseMultipleCouponDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    //订单总金额
    @NotNull(message = "订单总金额不能为空")
    private BigDecimal totalAmount;

    @NotEmpty(message = "优惠券ID不能为空")
    private List<Long> couponIds;
}
