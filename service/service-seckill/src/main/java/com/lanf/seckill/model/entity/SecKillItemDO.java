package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 秒杀商品表
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Data
@TableName("sec_kill_item")
public class SecKillItemDO extends BaseEntity {

private static final long serialVersionUID=1L;

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
    /**
     * 0：下架，1：上架
     */
    private Integer shelfStatus;

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

    @ApiModelProperty(value = "已售数量")
    private Integer soldStock;

    private Long merchantId;
    /**
     *
     */
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "skuId")
    private Long skuId;

    //商品版本
    private Long goodsVersion;
    //sku 版本
    private Long skuVersion;

    private Long version;



}
