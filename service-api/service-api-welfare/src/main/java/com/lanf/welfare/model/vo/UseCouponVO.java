package com.lanf.welfare.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class UseCouponVO implements Serializable {


    //折扣金额
    private BigDecimal discountMoney;
    private Long shopId;
    private Long couponId;

    public UseCouponVO() {
    }

    public UseCouponVO(BigDecimal discountMoney, Long shopId, Long couponId) {
        this.discountMoney = discountMoney;
        this.shopId = shopId;
        this.couponId = couponId;
    }
}



