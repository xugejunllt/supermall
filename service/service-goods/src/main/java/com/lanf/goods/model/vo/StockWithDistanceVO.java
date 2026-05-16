package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockWithDistanceVO implements Serializable {

    private String skuCode;


    private Long warehouseId;
    /**
     * 可用库存
     */
    private Integer usableStock;
    /**
     * 是否有货
     */
    private Boolean hasStock;
}
