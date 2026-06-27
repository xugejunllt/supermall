package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.enums.OrderAutoCloseStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 订单签收后的自动关闭超时时间
 * </p>
 *
 * @author jarven
 * @since 2026-06-27
 */
@Data
@TableName("order_auto_close")
public class OrderAutoClose extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Long userId;

    /**
     * 自动关闭超时时间点
     */
    private Date autoCloseTime;

    private OrderAutoCloseStatusEnum status;

    private Long tenantId;


}
