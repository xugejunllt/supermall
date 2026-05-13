package com.lanf.api.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockDetail implements Serializable {

    /** sku编码 */
    private String skuCode;

    /** 单位 */
    private String unit;

    /** 可使用库存 */
    private Integer usableStock;

    /** 锁住的库存 */
    private Integer lockStock;

    /** 仓库id */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;




}
