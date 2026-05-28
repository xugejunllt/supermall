package com.lanf.welfare.model.dto;

import com.lanf.welfare.model.bo.DiscountUseConditionBO;
import com.lanf.welfare.model.bo.FullDiscountUseConditionBO;
import com.lanf.welfare.model.bo.NoConditionUseConditionBO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class AddCouponTemplateDTO implements Serializable {

    /**
     * 用途 0:店铺优惠卷,1:注册发放 ,2:会员等级升级
     * 1、2只能由平台管理员发放
     *
     */
    @NotNull(message = "用途不能为空")
    private Integer purpose;

    /**
     * 店铺id purpose =0有值
     *
     */
    private Long shopId;

    @ApiModelProperty(value = "优惠券名称")
    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    @ApiModelProperty(value = "副标题")
    @NotBlank(message = "副标题不能为空")
    private String title;

    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷,2:固定金额券")
    @NotNull(message = "优惠券类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "发放总量")
    @NotNull(message = "发放总量不能为空")
    private Integer totalCount;

    @ApiModelProperty(value = "每个用户可领取的次数")
    @NotNull(message = "每个用户可领取的次数不能为空")
    private Integer receiveCount;

    @ApiModelProperty(value = "领取开始时间")
    @NotNull(message = "领取开始时间能为空")
    private Date receiveStartTime;

    @ApiModelProperty(value = "领取结束时间")
    @NotNull(message = "领取结束时间不能为空")
    private Date receiveEndTime;

    @ApiModelProperty(value = "使用开始时间")
    @NotNull(message = "使用开始时间不能为空")
    private Date useStartTime;

    @ApiModelProperty(value = "使用结束时间")
    @NotNull(message = "使用结束时间不能为空")
    private Date useEndTime;

    /**
     * 使用条件 不同优惠卷类型不同的使用条件
     */
    private DiscountUseConditionBO discountUseCondition;

    private FullDiscountUseConditionBO fullDiscountUseCondition;

    private NoConditionUseConditionBO noConditionUseCondition;

    //用户分布式锁
    private Long adminUserId;



}
