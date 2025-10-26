package com.lanf.welfare.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 优惠券
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
@Data
@TableName("coupon")
public class CouponDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long shopId;

    private Long userId;

    @ApiModelProperty(value = "优惠券模板id")
    private Long templateId;

    @ApiModelProperty(value = "过期时间")
    private Date endTime;

    @ApiModelProperty(value = "0：未使用,1:已使用")
    private Integer used;





}
