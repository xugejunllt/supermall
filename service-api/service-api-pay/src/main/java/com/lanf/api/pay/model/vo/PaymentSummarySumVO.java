package com.lanf.api.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PaymentSummarySumVO implements Serializable {

    private BigDecimal incomeAmount;

    private BigDecimal paymentAmount;

    private BigDecimal netIncome;             // 实际收入金额（净收入）
}
