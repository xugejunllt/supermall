package com.lanf.seckill.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddSeckillItemDTO implements Serializable {


    @ApiModelProperty(value = "所属活动ID")
    private Long activityId;

    @ApiModelProperty(value = "原始商品ID（关联基础商品库）")
    private Long itemId;

    @ApiModelProperty(value = "商品标题（可冗余）")
    private String itemTitle;

    @ApiModelProperty(value = "商品图片URL 主图展示")
    private String itemImage;

    @ApiModelProperty(value = "商品详情页的“多张图片")
    private String images;

    private String skuCode;

    private Long warehouseId;

    @ApiModelProperty(value = "属性")
    private String attributes;

    @ApiModelProperty(value = "原价")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "秒杀价")
    private BigDecimal seckillPrice;

    @ApiModelProperty(value = "总库存（对应Redis预加载数量）")
    private Integer totalStock;

    @ApiModelProperty(value = "每人限购数量")
    private Integer limitPerUser;


}
