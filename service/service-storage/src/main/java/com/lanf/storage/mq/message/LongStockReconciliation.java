package com.lanf.storage.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class LongStockReconciliation implements Serializable {


    private Long stockFlowId ;
    private Long orderId;

    private Integer quantity;

    private String skuCode;

    private Long warehouseId;

}
