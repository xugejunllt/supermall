package com.lanf.goods.model.query;

import com.lanf.constant.enums.goods.UserStockFlowEventTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationStockFlowQuery implements Serializable {

    private Long orderId;

    private UserStockFlowEventTypeEnum userStockFlowEventType;
}
