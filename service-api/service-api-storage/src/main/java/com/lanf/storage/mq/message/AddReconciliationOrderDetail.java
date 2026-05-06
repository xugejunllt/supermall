package com.lanf.storage.mq.message;

import com.lanf.goods.model.enums.UserStockFlowEventTypeEnum;
import com.lanf.order.model.enums.OrderStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddReconciliationOrderDetail implements Serializable {

    private Long orderId;

    private OrderStatusEnum toOrderStatus;

    private UserStockFlowEventTypeEnum userStockFlowEventType;

}
