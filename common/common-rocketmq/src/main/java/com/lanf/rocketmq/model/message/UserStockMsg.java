package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserStockMsg implements Serializable {


    private Long stockFlowId;
    //sku编码
    private String skuCode;

    //商品名称
    private String goodsName;

    //仓库id
    private Long warehouseId;

    //仓库名称
    private String warehouseName;

    //单位
    private String unit;

    //新增库存数量
    private Integer actualQuantity;


    /**
     * 填充字段
     */
    private Long userStockId;

}
