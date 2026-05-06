package com.lanf.storage.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationOrderDetailBO implements Serializable {
    private Integer quantity;

    private String skuCode;

    private Long warehouseId;
}
