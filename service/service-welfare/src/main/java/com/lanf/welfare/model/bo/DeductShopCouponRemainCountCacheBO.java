package com.lanf.welfare.model.bo;

import lombok.Data;

import java.io.Serializable;


@Data
public class DeductShopCouponRemainCountCacheBO implements Serializable {

    /**
     * -1： key不存在
     *  0：数量不足
     *  1：扣減成功
     */
    private Integer resultStatus;

}
