package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.enums.OrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 订单状态溯源表
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
@Data
@TableName("order_status_trace")
public class OrderStatusTraceDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    private OrderStatusEnum fromStatus;

    @ApiModelProperty(value = "变更后状态（同状态枚举）")
    private OrderStatusEnum toStatus;

    private String createDate;

    /**
     * 操作者 用户: user:123 管理员:admin:123 定时任务:system:123
     */
    private String operator;

    private String remark;




}
