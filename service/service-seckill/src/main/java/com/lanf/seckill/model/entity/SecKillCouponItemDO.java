package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 秒杀优惠券项目表
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Data
@TableName("sec_kill_coupon_item")
public class SecKillCouponItemDO extends BaseEntity {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "优惠券副标题")
    private String couponTitle;

    /**
     * 秒杀模式 0：实时秒杀，1：MQ排队秒杀
     */
    private SeckillModeEnum secKillMode;

    @ApiModelProperty(value = "总库存（对应Redis预加载数量）")
    private Integer totalStock;

    /**
     * 剩余库存 当秒杀模式为 MQ排队秒杀，进行扣减；
     * 初始化时 与totalStock数量一致
     */
    private Integer remainingStock;

    @ApiModelProperty(value = "每人限购数量")
    private Integer limitPerUser;

    @ApiModelProperty(value = "已售数量")
    private Integer soldStock;

    /**
     * 0：下架，1：上架
     */
    private Integer shelfStatus;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    private Long tenantId;

    private Long version;

}
