package com.lanf.welfare.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReceiveShopCouponDTO implements Serializable {
    //优惠卷模板id
    private Long couponTemplateId;
    //店铺id
    private Long shopId;
}
