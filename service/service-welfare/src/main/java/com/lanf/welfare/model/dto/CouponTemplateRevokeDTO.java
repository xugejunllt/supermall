package com.lanf.welfare.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CouponTemplateRevokeDTO {

    @NotNull(message = "优惠券模板ID不能为空")
    private Long couponTemplateId;


}