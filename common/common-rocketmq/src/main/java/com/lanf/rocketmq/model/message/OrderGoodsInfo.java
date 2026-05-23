package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderGoodsInfo implements Serializable {

    private Long goodsId;

    private String skuCode;



    private Long warehouseId;
    private Long tenantId;
}
