package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum;
import lombok.Data;

/**
 * <p>
 * 库存对账订单详细
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
@Data
@TableName("reconciliation_order_detail")
public class ReconciliationOrderDetailDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private String bathId;

    private Long orderId;

    private ReconciliationOrderStatusEnum orderStatus;

    private String orderItems;

    private String stockFlows;



}
