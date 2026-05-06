package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationOrderItem implements Serializable {

    private Integer quantity;

    private String skuCode;

    private Long warehouseId;


}
