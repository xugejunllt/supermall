package com.lanf.goods.model.query;

import com.lanf.goods.model.enums.UserStockFlowEventTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationStockFlowQuery implements Serializable {

    private Long orderId;

    private UserStockFlowEventTypeEnum userStockFlowEventType;
}
