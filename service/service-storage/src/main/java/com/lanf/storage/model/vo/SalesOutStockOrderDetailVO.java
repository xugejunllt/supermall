package com.lanf.storage.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SalesOutStockOrderDetailVO implements Serializable {


    private Long id;
    @ApiModelProperty(value = "单据编码")
    private String code;
    //总的预计入库量
    private Integer totalExpectStorageQuantity;
    //总的实际入库量
    private Integer totalActualStorageQuantity;
    //总的剩余数量
    private Integer totalActualSurplusQuantity;
    private String warehouseName;
    private List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList;
}


