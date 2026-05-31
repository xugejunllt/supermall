package com.lanf.welfare.model.vo;

import com.lanf.welfare.model.bo.DiscountUseConditionBO;
import com.lanf.welfare.model.bo.FullDiscountUseConditionBO;
import com.lanf.welfare.model.bo.NoConditionUseConditionBO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * 优惠券  一张优惠卷 一条记录
 *
 *
 * @author
 * @since 2024-08-01
 */
@Data
public class CouponPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty(value = "副标题")
    private String title;

    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷,2:固定金额券")
    private Integer type;

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

    private String useCondition;

}
