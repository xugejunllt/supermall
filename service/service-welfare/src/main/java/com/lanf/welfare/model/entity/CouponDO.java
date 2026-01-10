package com.lanf.welfare.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 优惠券
 * </p>
 *
 * @author
 * @since 2024-08-01
 */
@Data
@TableName("coupon")
public class CouponDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模板id")
    private Long couponTemplateId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 优惠券的发放类型 0:店铺优惠卷, 1:注册发放 , 2:会员等级升级
     * 1、2只能由平台管理员发放
     */
    private Integer couponType;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "副标题")
    private String title;

    @ApiModelProperty(value = "状态 0: 待使用 1：已使用 2：作废 作废 不能使用")
    private Integer status;

    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷,2:固定金额券")
    private Integer type;

    @ApiModelProperty(value = "使用条件")
    private String useCondition;

    @ApiModelProperty(value = "使用开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "使用结束时间")
    private Date useEndTime;

    @ApiModelProperty(value = "优惠卷模板版本号")
    private Long couponTemplateVersion;

    private Long version;



}
