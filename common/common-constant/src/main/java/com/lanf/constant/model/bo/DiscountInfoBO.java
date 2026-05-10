package com.lanf.constant.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DiscountInfoBO implements Serializable {

    //优惠卷id
    private Long couponId;
    //优惠券名称
    private String name;
    //副标题
    private String title;
    //优惠金额
    private BigDecimal discountAmount;
    //0:满减劵,1:抵扣卷,2:固定金额券
    private Integer type;

}
