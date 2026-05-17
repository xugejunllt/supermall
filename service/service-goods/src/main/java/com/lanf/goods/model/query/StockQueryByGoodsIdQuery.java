package com.lanf.goods.model.query;

import com.lanf.goods.model.enums.WarehouseSelectionStrategyEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StockQueryByGoodsIdQuery implements Serializable {


    private Long goodsId;

    private String areaCode;

    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;

    private WarehouseSelectionStrategyEnum warehouseSelectionStrategy;

}
