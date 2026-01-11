package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CalculateOrderAmountVO implements Serializable {

    //订单总金额
    private BigDecimal totalAmount;

    //实际支付金额
    private BigDecimal payAmount;

    //优惠金额
    private BigDecimal discountAmount;
    /**
     * 优惠卷信息
     */
}
