package com.lanf.api.storage.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PurchaseInStockOrderPageVO implements Serializable {

    private Long id;
    /** 单据编码 */
    private String code;

    //供应商名称
    private String supplierName;

    /** 预计入库数量 */
    private Integer expectStorageQuantity;

    /** 实际入库数量 */
    private Integer actualStorageQuantity;

    /** 入库状态0:待入库,1:部分入库 2:已入库 */
    private Integer storageStatus;


    /** 仓库名称 */
    private String warehouseName;
    /** 仓库id */
    private Long warehouseId;
    private Long supplierId;
    private Date createTime;


}
