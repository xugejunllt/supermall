package com.lanf.welfare.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopCouponRemainCountCacheBO implements Serializable {

    private Long couponTemplateId;

    private Integer remainCount;



}
