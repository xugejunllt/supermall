package com.lanf.welfare.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ReceiveShopCouponDTO implements Serializable {

    //优惠卷模板id
    @NotNull(message = "优惠券模板id不能为空")
    private Long couponTemplateId;
    //店铺id
    @NotNull(message = "店铺id不能为空")
    private Long shopId;

    private Long userId;
}
