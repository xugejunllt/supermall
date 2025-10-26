package com.lanf.storage.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OutStockDTO implements Serializable {

    //销售出库单id
    private Long salesOutStockOrderId;

    private List<OutStockItemDTO> outStockItemList;
}
