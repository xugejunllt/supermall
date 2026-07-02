package com.lanf.seckill.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 秒杀商品分页查询结果VO
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Data
public class SecKillItemPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long id;

    @ApiModelProperty(value = "所属活动ID")
    private Long activityId;

    @ApiModelProperty(value = "商品标题")
    private String itemTitle;

    @ApiModelProperty(value = "商品图片URL")
    private String itemImage;

    @ApiModelProperty(value = "原价")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "秒杀价")
    private BigDecimal seckillPrice;

    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "剩余库存")
    private Integer remainingStock;

    @ApiModelProperty(value = "已售数量")
    private Integer soldStock;

    @ApiModelProperty(value = "上架状态：0-下架，1-上架")
    private Integer shelfStatus;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

}
