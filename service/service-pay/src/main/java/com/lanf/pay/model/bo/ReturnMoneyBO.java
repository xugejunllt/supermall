package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ReturnMoneyBO implements Serializable {

    //交易号
    private String tradeNo;

    //退款金额
    private BigDecimal refundMoney;
}
