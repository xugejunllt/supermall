package com.lanf.pay.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RechargeDTO implements Serializable {
    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;
}
