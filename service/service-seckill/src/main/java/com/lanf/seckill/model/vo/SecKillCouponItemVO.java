package com.lanf.seckill.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀优惠券列表VO
 */
@Data
public class SecKillCouponItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "秒杀优惠券ID")
    private Long secKillCouponItemId;

    @ApiModelProperty(value = "所属活动ID")
    private Long activityId;

    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "优惠券副标题")
    private String couponTitle;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "剩余库存")
    private Integer remainingStock;

}
