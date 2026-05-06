package com.lanf.storage.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BuildReconciliationOrderDetailMessage implements Serializable {

    private String bathId;

    private Long maxOrderId;
    private List<ReconciliationOrderDetail> orderDetails;

}
