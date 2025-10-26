package com.lanf.aftersales.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExchangeGoodsCreateOutStockOrderDTO implements Serializable {

    private Long afterSalesOrderId;

    private List<ExchangeGoodsCreateOutStockOrderItemDTO> list;
}
