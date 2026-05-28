package com.lanf.api.storage.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AfterSalesIntStockOrderPageVO implements Serializable {

    private Long id;

    /** 单据编码 */
    private String code;

    /** 出库状态0:待入库, 1:已入库 */
    private Integer storageStatus;
}
