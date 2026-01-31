package com.lanf.order.model.vo;

import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CalculateOrderAmountVO implements Serializable {

    // 订单编号
    private String orderNumber;

    //订单总金额
    private BigDecimal totalAmount;

    //实际支付金额
    private BigDecimal payAmount;

    //优惠金额
    private BigDecimal discountAmount;
    /**
     * 优惠卷信息
     */
    private CalculateDiscountAmountVO calculateDiscountAmountVO;

}
