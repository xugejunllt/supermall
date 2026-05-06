package com.lanf.storage.mq.message;

import com.lanf.storage.model.enums.ReconciliationOrderStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class BuildReconciliationOrderDetailMessage implements Serializable {


    private Long orderId;

    private ReconciliationOrderStatusEnum orderStatus;
}
