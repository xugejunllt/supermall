package com.lanf.storage.service.storage.impl.manager;

import com.lanf.api.storage.model.bo.CalculatePurchaseOrderMoneyBO;
import com.lanf.api.storage.model.dto.CalculatePurchaseOrderItemMoneyDTO;
import com.lanf.api.storage.model.dto.CalculatePurchaseOrderMoneyDTO;

import java.math.BigDecimal;

public interface StorageAdapter {

    CalculatePurchaseOrderMoneyBO calculatePurchaseOrderMoney(CalculatePurchaseOrderMoneyDTO calculatePurchaseOrderMoney);
    BigDecimal calculateItemTotalMoney(CalculatePurchaseOrderItemMoneyDTO purchaseOrderItemMoney);
}
