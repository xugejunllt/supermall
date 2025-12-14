package com.lanf.welfare.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 优惠卷作废
 * </p>
 *
 * @author jarven
 * @since 2025-12-14
 */
@Data
@TableName("coupon_template_revoke")
public class CouponTemplateRevokeDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "优惠卷模板id")
    private Long couponTemplateId;

    @ApiModelProperty(value = "0：作废中，1：已完成，当所有用户优惠卷已作废时变成已完成")
    private Integer status;




}
