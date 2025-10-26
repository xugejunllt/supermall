package com.lanf.finance.model.vo.pay;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FinancialDetailsSummaryVO implements Serializable {

    //收入金额
    private BigDecimal incomeMoney;
    //支出金额
    private BigDecimal payOutMoney;
    //收入总笔数
    private Integer incomeTotalCount;
    //支出总笔数
    private Integer payOutTotalCount;
    //当日余额
    private BigDecimal remainMoney;

}
