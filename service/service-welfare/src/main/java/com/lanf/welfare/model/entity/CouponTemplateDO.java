package com.lanf.welfare.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 优惠券模板
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
@Data
@TableName("coupon_template")
public class CouponTemplateDO extends BaseEntity {

private static final long serialVersionUID=1L;



    /**
     * 用途 0:店铺优惠卷,1:注册发放 ,2:会员等级升级
     * 1、2只能由平台管理员发放
     *
     */
    private Integer purpose;

    /**
     * 店铺id purpose =0有值
     *
     */
    private Long shopId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "副标题")
    private String title;
    /**
     * 状态 0: 待发布 1：已发布 2：作废
     * 作废 不能领取 也不能使用
     */
    private Integer status;

    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷,2:固定金额券")
    private Integer type;

    @ApiModelProperty(value = "发放总量")
    private Integer totalCount;

    @ApiModelProperty(value = "剩余数量")
    private Integer remainCount;

    @ApiModelProperty(value = "每个用户可领取的次数")
    private Integer receiveCount;

    @ApiModelProperty(value = "使用条件")
    private String useCondition;

    @ApiModelProperty(value = "领取开始时间")
    private Date receiveStartTime;

    @ApiModelProperty(value = "领取结束时间")
    private Date receiveEndTime;

    @ApiModelProperty(value = "使用开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "使用结束时间")
    private Date useEndTime;
    private Long version;

    private Long tenantId;





}
