package com.lanf.welfare.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReceiveCouponDTO implements Serializable {

    //优惠券id
    private Long couponTemplateId;
}
