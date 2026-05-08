package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.seckill.model.enums.SecKillOrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 秒杀订单表
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Data
@TableName("sec_kill_order")
public class SecKillOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀商品ID（seckill_item 表的 id）")
    private Long itemId;

    @ApiModelProperty(value = "活动ID")
    private Long activityId;

    @ApiModelProperty(value = "业务订单号")
    private String orderNumber;

    @ApiModelProperty(value = "秒杀商品数量")
    private Integer itemQuantity;

    private SecKillOrderStatusEnum orderStatus;


    private Long merchantId;


}
