package com.lanf.storage.model.vo;

import lombok.Data;

import java.io.Serializable;


@Data
public class StockVO implements Serializable {

    private String skuCode;

    private Integer usableStock;
}
