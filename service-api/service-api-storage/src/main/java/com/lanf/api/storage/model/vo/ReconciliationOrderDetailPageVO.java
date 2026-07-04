package com.lanf.api.storage.model.vo;

import com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReconciliationOrderDetailPageVO implements Serializable {

    private Long id;

    /** 批次id */
    private String bathId;

    /** 订单id */
    private Long orderId;

    /** 订单状态 */
    private ReconciliationOrderStatusEnum orderStatus;

    /** 订单商品 */
    private String orderItems;

    /** 库存流水 */
    private String stockFlows;

    private Date createTime;

    private Date updateTime;
}
