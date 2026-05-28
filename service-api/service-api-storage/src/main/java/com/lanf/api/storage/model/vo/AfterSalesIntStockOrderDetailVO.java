package com.lanf.api.storage.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AfterSalesIntStockOrderDetailVO implements Serializable {


    private List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList;

}
