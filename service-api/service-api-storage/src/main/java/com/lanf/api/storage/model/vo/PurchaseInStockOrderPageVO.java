package com.lanf.api.storage.model.vo;

import com.lanf.api.storage.model.enums.StorageStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PurchaseInStockOrderPageVO implements Serializable {

    private Long id;
    /** 单据编码 */
    private String code;

    /** 预计入库数量 */
    private Integer expectStorageQuantity;

    /** 实际入库数量 */
    private Integer actualStorageQuantity;

    private StorageStatusEnum storageStatus;

    private Date createTime;


}
