package com.lanf.welfare.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class UseCouponDTO implements Serializable {

    private Long userId;
    //优惠券id
    private Long couponId;
    //订单金额
    private BigDecimal orderMoney;

}
