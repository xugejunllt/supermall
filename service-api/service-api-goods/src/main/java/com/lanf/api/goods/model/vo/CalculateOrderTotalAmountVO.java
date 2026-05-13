package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CalculateOrderTotalAmountVO implements Serializable {


    //订单总金额
    private BigDecimal totalAmount;
}
