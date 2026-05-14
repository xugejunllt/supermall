package com.lanf.api.storage.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SalesOutStockOrderAddDTO implements Serializable {

    private Integer expectOutQuantity;
    /**
     * 物流公司
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String expressNumber;

    private List<InOutStockOrderItemDTO> inOutStockOrderItemDTOList;


}
