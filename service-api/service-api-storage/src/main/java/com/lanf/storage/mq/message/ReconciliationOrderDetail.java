package com.lanf.storage.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReconciliationOrderDetail implements Serializable {


    private String bathId;

    private Long orderId;

    private List<ReconciliationOrderDetailItem> reconciliationOrderDetailItems;
}
