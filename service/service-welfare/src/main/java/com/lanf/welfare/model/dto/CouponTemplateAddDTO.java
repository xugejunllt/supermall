package com.lanf.welfare.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponTemplateAddDTO implements Serializable {



    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "副标题")
    private String title;

    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷")
    private Integer type;

    @ApiModelProperty(value = "每个用户可领取的次数")
    private Integer receiveCount;

    @ApiModelProperty(value = "满足金额")
    private BigDecimal meetMoney;

    @ApiModelProperty(value = "折扣金额")
    private BigDecimal discountMoney;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;
    @DateTimeFormat
    @ApiModelProperty(value = "结束时间")
    private Date endTime;

}
