package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DeductStockVO implements Serializable {

    //订单总金额
    private  BigDecimal totalAmount;
}
