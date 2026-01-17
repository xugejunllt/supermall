package com.lanf.welfare.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Deprecated
@Data
public class UseMultipleCouponDTO2 implements Serializable {

    //优惠卷id
    private List<Long> couponId;
    //订单总金额
    private BigDecimal totalPrice;


}
