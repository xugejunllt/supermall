package com.lanf.welfare.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lanf.common.utils.DateUtils;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
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

    @JsonFormat(pattern = DateUtils.DATE)
    @ApiModelProperty(value = "开始时间")
    private Date startTime;
    @JsonFormat(pattern = DateUtils.DATE)
    @ApiModelProperty(value = "结束时间")
    private Date endTime;



}
