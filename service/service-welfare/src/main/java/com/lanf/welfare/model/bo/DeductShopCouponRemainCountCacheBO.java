package com.lanf.welfare.model.bo;

import lombok.Data;

import java.io.Serializable;


@Data
public class DeductShopCouponRemainCountCacheBO implements Serializable {

    //key 是否存在
    private Boolean exist;
    // 优惠券是否扣减成功
    private Boolean deductOk;

}
