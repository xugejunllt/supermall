package com.lanf.seckill.model.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SeckillItemVO implements Serializable {

    @ApiModelProperty(value = "所属活动ID")
    private Long activityId;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long seckillItemId;

    @ApiModelProperty(value = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "商品标题")
    private String itemTitle;

    @ApiModelProperty(value = "商品主图URL")
    private String itemImage;

    @ApiModelProperty(value = "商品属性（JSON格式）")
    private String attributes;

    @ApiModelProperty(value = "原价")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "秒杀价")
    private BigDecimal seckillPrice;
}
