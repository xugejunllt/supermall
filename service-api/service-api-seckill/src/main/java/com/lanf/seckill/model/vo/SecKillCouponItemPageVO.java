package com.lanf.seckill.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 秒杀优惠券项目分页查询结果VO
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Data
public class SecKillCouponItemPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券项目ID")
    private Long id;

    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "优惠券副标题")
    private String couponTitle;

    @ApiModelProperty(value = "秒杀模式 0：实时秒杀，1：MQ排队秒杀")
    private Integer secKillMode;

    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "剩余库存")
    private Integer remainingStock;

    @ApiModelProperty(value = "每人限购数量")
    private Integer limitPerUser;

    @ApiModelProperty(value = "已售数量")
    private Integer soldStock;

    @ApiModelProperty(value = "上架状态：0-下架，1-上架")
    private Integer shelfStatus;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
