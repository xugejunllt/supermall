package com.lanf.welfare.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 优惠卷使用记录 一张优惠卷 一条记录
 * </p>
 *
 * @author jarven
 * @since 2026-01-17
 */
@Data
@TableName("order_coupon")
public class OrderCouponDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "优惠卷id")
    private Long couponId;




}
