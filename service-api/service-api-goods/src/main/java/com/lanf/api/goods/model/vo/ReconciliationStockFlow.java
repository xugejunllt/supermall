package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 对账库存流水
 */
@Data
public class ReconciliationStockFlow implements Serializable {

    private Integer quantity;

    private String skuCode;

    private Long warehouseId;

}
