package com.lanf.seckill.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀优惠券Token VO
 */
@Data
public class SecKillCouponTokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "秒杀令牌")
    private String token;

    @ApiModelProperty(value = "动态下单链接")
    private String orderUrl;

}
