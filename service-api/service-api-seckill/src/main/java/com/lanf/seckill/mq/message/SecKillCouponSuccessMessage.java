package com.lanf.seckill.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 秒杀优惠券成功消息
 * 优惠券服务消费此消息，插入用户优惠券
 */
@Data
public class SecKillCouponSuccessMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀优惠券ID")
    private Long secKillCouponItemId;

    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ApiModelProperty(value = "店铺ID")
    private Long shopId;

    @ApiModelProperty(value = "店铺名称")
    private String shopName;

}
