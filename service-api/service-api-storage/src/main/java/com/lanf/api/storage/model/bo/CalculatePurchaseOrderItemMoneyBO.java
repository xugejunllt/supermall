package com.lanf.api.storage.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CalculatePurchaseOrderItemMoneyBO implements Serializable {

    //sku编码,库存最小单位
    private String skuCode;

    //商品项目总金额
    private BigDecimal itemTotalMoney;


}
