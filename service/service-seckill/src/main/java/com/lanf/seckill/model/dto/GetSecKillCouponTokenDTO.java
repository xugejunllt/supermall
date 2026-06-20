package com.lanf.seckill.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 获取秒杀优惠券Token DTO
 */
@Data
public class GetSecKillCouponTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "秒杀优惠券ID不能为空")
    private Long secKillCouponItemId;

}
