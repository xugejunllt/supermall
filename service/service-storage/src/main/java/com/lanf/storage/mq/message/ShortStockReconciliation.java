package com.lanf.storage.mq.message;

import com.lanf.storage.model.bo.ReconciliationOrderDetailBO;
import com.lanf.storage.model.enums.ReconciliationOrderStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShortStockReconciliation implements Serializable {

    private Long orderId;

    private ReconciliationOrderStatusEnum orderStatus;

    private List<ReconciliationOrderDetailBO> orderItems;

    private List<ReconciliationOrderDetailBO> stockFlows;
}
