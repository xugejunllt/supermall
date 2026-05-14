package com.lanf.storage.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SalesOutStockOrderPageVO implements Serializable {

    private Long id;

    /** 单据编码 */
    private String code;

    private Integer inOutStatus;

    /** 预计出库数量 */
    private Integer expectQuantity;

    /** 实际出库数量 */
    private Integer actualQuantity;

    /** 出库状态0:待出库,1:部分出库 2:已出库 */
    private Integer storageStatus;

    /** 仓库名称 */
    private String warehouseName;

    /** 备注 */
    private String remarks;
    /** 订单id */
    private Long orderId;
    /** 仓库id */
    private Long warehouseId;

    private Date createTime;
}
