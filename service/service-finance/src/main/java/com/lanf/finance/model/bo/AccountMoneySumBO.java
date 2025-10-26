package com.lanf.finance.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AccountMoneySumBO implements Serializable {


    @ApiModelProperty(value = "账户类型0:支付宝")
    private Integer accountType;
    //科目名称
    private String incomeSubjectName;
    @ApiModelProperty(value = "收支账户")
    private String incomeAccount;
    //总金额
    private BigDecimal sumMoney;

}
