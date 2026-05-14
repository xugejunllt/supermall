package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OutStockSalesOutStockOrderDTO implements Serializable {

    //销售出库单id
    private Long salesOutStockOrderId;
    private Long warehouseId;

    private List<OutStockItemDTO> outStockItemList;
}
