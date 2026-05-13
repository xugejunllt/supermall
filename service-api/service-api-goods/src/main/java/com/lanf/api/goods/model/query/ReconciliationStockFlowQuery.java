package com.lanf.api.goods.model.query;

import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 对账库存流水查询
 */
@Data
public class ReconciliationStockFlowQuery implements Serializable {

    private Long orderId;

    private UserStockFlowEventTypeEnum userStockFlowEventType;
}
