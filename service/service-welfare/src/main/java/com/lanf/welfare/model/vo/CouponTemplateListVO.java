package com.lanf.welfare.model.vo;

import com.lanf.welfare.model.bo.DiscountUseConditionBO;
import com.lanf.welfare.model.bo.FullDiscountUseConditionBO;
import com.lanf.welfare.model.bo.NoConditionUseConditionBO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CouponTemplateListVO implements Serializable {


    private Long id;
    @ApiModelProperty(value = "优惠券名称")
    private String name;

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

    /**
     * 使用条件 不同优惠卷类型不同的使用条件
     */
    private DiscountUseConditionBO discountUseCondition;

    private FullDiscountUseConditionBO fullDiscountUseCondition;

    private NoConditionUseConditionBO noConditionUseCondition;

}
