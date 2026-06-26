package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

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

    private static final long serialVersionUID = 1L;

    private Long shopId;

    private String shopName;

    @ApiModelProperty(value = "所属活动ID")
    private Long activityId;

    @ApiModelProperty(value = "原始商品ID（关联基础商品库）")
    private Long itemId;
    /**
     * 秒杀模式 0：实时秒杀，1：MQ排队秒杀
     */
    private SeckillModeEnum secKillMode;

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
    /**
     *剩余库存 当秒杀模式为 MQ排队秒杀，进行扣减；
     * 初始化时 与totalStock数量一致
     */
    private Integer remainingStock;

    @ApiModelProperty(value = "每人限购数量")
    private Integer limitPerUser;

    @ApiModelProperty(value = "已售数量")
    private Integer soldStock;

    private Long tenantId;
    /**
     *
     */
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "skuId")
    private Long skuId;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    private Long version;


}
