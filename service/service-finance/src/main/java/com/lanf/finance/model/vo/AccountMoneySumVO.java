package com.lanf.finance.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AccountMoneySumVO implements Serializable {



    //收入总金额
    private BigDecimal incomeSumMoney;
    //支出总金额
    private BigDecimal payOutSumMoney;
    //变更总金额
    private  BigDecimal changeSumMoney;


}
