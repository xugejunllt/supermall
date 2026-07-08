package com.lanf.pay.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WithdrawApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提现金额
     */
    @NotNull(message = "提现金额不能为空")
    private BigDecimal amount;

    /**
     * 收款账户（支付宝账号/银行卡号）
     */
    @NotNull(message = "收款账户不能为空")
    private String payeeAccount;

    /**
     * 提现方式：1-支付宝，2-银行卡
     */
    @NotNull(message = "提现方式不能为空")
    private Integer withdrawType;

    @NotNull(message = "收款账户名称不能为空")
    private String incomeAccountUserName;
    /**
     * 备注
     */
    private String remark;
}
