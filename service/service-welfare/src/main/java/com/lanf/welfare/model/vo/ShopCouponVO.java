package com.lanf.welfare.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lanf.common.utils.DateUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ShopCouponVO implements Serializable {

    private Long couponId;

    @ApiModelProperty(value = "优惠券模板id")
    private Long templateId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "副标题")
    private String title;

    @ApiModelProperty(value = " 0:满减劵,1:抵扣卷")
    private Integer type;

    @ApiModelProperty(value = "满足金额")
    private BigDecimal meetMoney;

    @ApiModelProperty(value = "折扣金额")
    private BigDecimal discountMoney;
    @JsonFormat(pattern = DateUtils.DATE)
    @ApiModelProperty(value = "开始时间")
    private Date startTime;
    @JsonFormat(pattern = DateUtils.DATE)
    @ApiModelProperty(value = "结束时间")
    private Date endTime;



}
