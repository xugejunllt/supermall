package com.lanf.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InStockItemDTO implements Serializable {

    //storage_order_item_details id
    private Long id;

    private String skuCode;
    //实际入库数量
    private Integer actualQuantity;

}
