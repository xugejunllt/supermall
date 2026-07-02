package com.lanf.seckill.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 秒杀优惠券记录分页查询结果VO
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Data
public class SecKillCouponRecordPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀优惠券项目ID")
    private Long secKillCouponItemId;

    @ApiModelProperty(value = "秒杀的库存数量")
    private Integer stockQuantity;

    @ApiModelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ApiModelProperty(value = "状态：0-秒杀成功，1-优惠券已发放，2-秒杀失败")
    private Integer status;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
