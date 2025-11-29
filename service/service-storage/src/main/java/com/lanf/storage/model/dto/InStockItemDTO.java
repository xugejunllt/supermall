package com.lanf.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InStockItemDTO implements Serializable {

    private Long id;

    private String skuCode;
    //实际入库数量
    private Integer actualQuantity;
    //仓库id
    private Long warehouseId;



}
