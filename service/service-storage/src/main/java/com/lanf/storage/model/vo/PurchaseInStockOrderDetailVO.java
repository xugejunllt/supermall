package com.lanf.storage.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseInStockOrderDetailVO implements Serializable {

    private Long id;
    /** 单据编码 */
    private String code;
    //总的预计入库量
    private Integer totalExpectStorageQuantity;
    //总的实际入库量
    private Integer totalActualStorageQuantity;
    //总的剩余数量
    private Integer totalActualSurplusQuantity;
    //供应商名称
    private String supplierName;
    /** 仓库名称 */
    private String warehouseName;
    private List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList;
}
