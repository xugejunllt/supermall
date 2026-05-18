package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class StockEnoughVO implements Serializable {


    private String skuCode;

    private Long goodsId;

    private Long warehouseId;

}
