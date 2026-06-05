package com.lanf.storage.model.bo;

import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddReconciliationOrderDetailBO implements Serializable {

    private Long orderId;

    private OrderStatusEnum toOrderStatus;

    private UserStockFlowEventTypeEnum userStockFlowEventType;

}
