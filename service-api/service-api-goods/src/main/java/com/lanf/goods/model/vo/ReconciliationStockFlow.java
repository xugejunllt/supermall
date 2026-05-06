package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationStockFlow implements Serializable {

    private Integer quantity;

    private String skuCode;

    private Long warehouseId;


}
