package com.lanf.seckill.model.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品列表
 */
@Data
public class SeckillItemList implements Serializable {

    @ApiModelProperty(value = "所属活动ID")
    private Long activityId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 秒杀商品ID
     */
    private Long seckillItemId;

    @ApiModelProperty(value = "商品标题（可冗余）")
    private String itemTitle;

    @ApiModelProperty(value = "商品图片URL 主图展示")
    private String itemImage;

    @ApiModelProperty(value = "属性")
    private String attributes;

    @ApiModelProperty(value = "原价")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "秒杀价")
    private BigDecimal seckillPrice;

}
