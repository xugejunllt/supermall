package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutStockItemDTO implements Serializable {

    private Long id;
    private String skuCode;
    //实际出入库数量
    private Integer actualQuantity;
}
