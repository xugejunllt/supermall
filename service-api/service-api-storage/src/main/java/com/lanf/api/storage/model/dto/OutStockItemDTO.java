package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutStockItemDTO implements Serializable {

    private Long outStockItemId;
    private String skuCode;
    private Integer actualQuantity;
}
