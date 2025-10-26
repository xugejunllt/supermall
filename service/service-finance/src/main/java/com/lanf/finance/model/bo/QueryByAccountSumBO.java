package com.lanf.finance.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class QueryByAccountSumBO implements Serializable {

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
    //数据来源
    private String dataSource;
}
