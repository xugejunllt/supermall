package com.lanf.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InStockItemDTO implements Serializable {

    private Long id;
    //sku编码
    private String skuCode;
    //实际入库数量
    private Integer actualQuantity;
    //仓库id
    private Long warehouseId;
    //商品名称
    private String goodsName;
    //商品单位
    private String unit;
    /**
     * 填充字段
     */
    /**
     * 仓储库存id
     */
    private Long stockFlowId;

}
