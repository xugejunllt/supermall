package com.lanf.welfare.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CacheCouponTemplateListBO implements Serializable {


    private Long id;
    @ApiModelProperty(value = "优惠券名称")
    private String name;
    /**
     * 状态 0: 待发布 1：已发布 2：作废
     * 作废 不能领取 也不能使用
     */
    private Integer status;

    @ApiModelProperty(value = "副标题")

    private String title;
    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷,2:固定金额券")
    private Integer type;

    @ApiModelProperty(value = "领取开始时间")
    private Date receiveStartTime;

    @ApiModelProperty(value = "领取结束时间")
    private Date receiveEndTime;

    @ApiModelProperty(value = "使用开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "使用结束时间")
    private Date useEndTime;
    @ApiModelProperty(value = "剩余数量")
    private Integer remainCount;
    /**
     * 使用条件 不同优惠卷类型不同的使用条件
     */
    private DiscountUseConditionBO discountUseCondition;

    private FullDiscountUseConditionBO fullDiscountUseCondition;

    private NoConditionUseConditionBO noConditionUseCondition;

}
