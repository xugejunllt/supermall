package com.lanf.client.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CreatePayOrderItemVO implements Serializable {

    private Long shopId;

    private BigDecimal totalMoney;
    //实际支付金额
    private BigDecimal actualPayMoney;
}
