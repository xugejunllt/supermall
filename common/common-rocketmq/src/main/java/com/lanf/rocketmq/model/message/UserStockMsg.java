package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserStockMsg implements Serializable {


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

    //同步时间,用于与仓储库存对账
    private Date syncTime;
}
