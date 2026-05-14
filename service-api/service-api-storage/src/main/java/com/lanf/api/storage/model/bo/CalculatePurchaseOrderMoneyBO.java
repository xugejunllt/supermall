package com.lanf.api.storage.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculatePurchaseOrderMoneyBO implements Serializable {


    //全部商品总金额
    private BigDecimal allItemTotalMoney;
    //采购单总费用
    private BigDecimal  purchaseOrderTotalMoney;

    private List<CalculatePurchaseOrderItemMoneyBO> calculatePurchaseOrderItemMoney;

}
