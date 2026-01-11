package com.lanf.welfare.model.query;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GetAvailableCouponQuery implements Serializable {

    //店铺id
    private Long shopId;

    //订单总金额
    private BigDecimal totalPrice;


}
