package com.lanf.api.storage.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockPageQueryVO implements Serializable {


    private Long id;
    /** sku编码 */
    private String skuCode;
    /**
     * 商品名称
     */
    private String goodsName;

    /** 仓库名称 */
    private String warehouseName;

    //单位
    private String unit;
    /**
     * 总库存
     */
    private Integer totalStock;
    /**
     * 可使用库存
     */
    private Integer usableStock;

    /**
     * 预售库存
     */
    private Integer preStock;

    /** 仓库id */
    private Long warehouseId;

}
