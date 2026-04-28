package com.lanf.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WithdrawApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "提现金额", required = true)
    @NotNull(message = "提现金额不能为空")
    private BigDecimal amount;

    @ApiModelProperty(value = "收款账户（支付宝账号/银行卡号）", required = true)
    @NotNull(message = "收款账户不能为空")
    private String payeeAccount;

    @ApiModelProperty(value = "提现方式：1-支付宝，2-银行卡", required = true)
    @NotNull(message = "提现方式不能为空")
    private Integer withdrawType;

    @ApiModelProperty(value = "备注")
    private String remark;
}
