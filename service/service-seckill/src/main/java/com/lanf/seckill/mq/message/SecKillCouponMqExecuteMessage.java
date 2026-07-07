package com.lanf.seckill.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 秒杀优惠券执行消息
 */
@Data
public class SecKillCouponMqExecuteMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀优惠券ID")
    private Long secKillCouponItemId;

    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    private SeckillModeEnum seckillModeEnum;

}
