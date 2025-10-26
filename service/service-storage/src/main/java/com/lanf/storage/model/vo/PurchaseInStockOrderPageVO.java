package com.lanf.storage.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PurchaseInStockOrderPageVO implements Serializable {

    private Long id;
    @ApiModelProperty(value = "单据编码")
    private String code;

    //供应商名称
    private String supplierName;

    @ApiModelProperty(value = "预计入库数量")
    private Integer expectStorageQuantity;

    @ApiModelProperty(value = "实际入库数量")
    private Integer actualStorageQuantity;

    @ApiModelProperty(value = "入库状态0:待入库,1:部分入库 2:已入库 ")
    private Integer storageStatus;


    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;
    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;
    private Long supplierId;
    private Date createTime;


}
