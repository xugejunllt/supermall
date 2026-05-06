package com.lanf.storage.mq.message;

import com.lanf.goods.model.enums.UserStockFlowEventTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationOrderDetailItem implements Serializable {


    private Long orderId;

    private Integer quantity;

    private String skuCode;

    private Long warehouseId;

    private UserStockFlowEventTypeEnum eventType;

}
