package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AfterSalesIntStockDTO implements Serializable {

    private Long id;

    private Long warehouseId;
}
