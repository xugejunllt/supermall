package com.lanf.finance.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 资金流水
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
@Data

public class MoneyFlowSumVO {

    //收入总金额
    private BigDecimal incomeSum;
    //支出总金额
    private BigDecimal payOutSum;



}
