package com.lanf.api.storage.model.query;

import com.lanf.api.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class ReconciliationOrderDetailPageQuery extends PageQuery {

    /** 批次id */
    private String bathId;

    /** 订单id */
    private Long orderId;

    /** 订单状态 */
    private ReconciliationOrderStatusEnum orderStatus;
}
