package com.lanf.seckill.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 添加秒杀优惠券DTO
 */
@Data
public class AddSecKillCouponItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;



    @NotNull(message = "优惠券模板ID不能为空")
    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @NotBlank(message = "优惠券名称不能为空")
    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "优惠券副标题")
    private String couponTitle;

    /**
     * 秒杀模式 0：实时秒杀，1：MQ排队秒杀
     */
    private SeckillModeEnum secKillMode;

    @NotNull(message = "总库存不能为空")
    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "每人限购数量，默认1")
    private Integer limitPerUser = 1;

    @ApiModelProperty(value = "秒杀价")
    private BigDecimal seckillPrice;

    @NotNull(message = "活动开始时间不能为空")
    @ApiModelProperty(value = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @NotNull(message = "活动结束时间不能为空")
    @ApiModelProperty(value = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

}
