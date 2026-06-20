package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 秒杀优惠券记录表
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Data
@TableName("sec_kill_coupon_record")
public class SecKillCouponRecordDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀优惠券项目ID")
    private Long secKillCouponItemId;

    @ApiModelProperty(value = "秒杀的库存数量")
    private Integer stockQuantity;

    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    /**
     * 0:秒杀成功，1:优惠券已发放，2:秒杀失败
     */
    private Integer status;

    private Long tenantId;

}
