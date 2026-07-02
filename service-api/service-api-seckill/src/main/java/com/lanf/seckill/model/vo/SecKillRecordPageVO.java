package com.lanf.seckill.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 秒杀记录分页查询结果VO
 * </p>
 *
 * @author jarven
 * @since 2026-05-09
 */
@Data
public class SecKillRecordPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long secKillItemId;

    @ApiModelProperty(value = "秒杀的库存数量")
    private Integer stockQuantity;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
