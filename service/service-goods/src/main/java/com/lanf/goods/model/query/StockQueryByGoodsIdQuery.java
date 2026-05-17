package com.lanf.goods.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockQueryByGoodsIdQuery implements Serializable {


    private Long goodsId;

    private String areaCode;
}
