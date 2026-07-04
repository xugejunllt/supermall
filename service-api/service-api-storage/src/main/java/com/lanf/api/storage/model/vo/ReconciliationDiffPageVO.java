package com.lanf.api.storage.model.vo;

import com.lanf.api.storage.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.api.storage.model.enums.ReconciliationJobTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReconciliationDiffPageVO implements Serializable {

    private Long id;

    /** 批次id */
    private String bathId;

    /** 订单id */
    private Long orderId;

    /** 商品sku编码 */
    private String skuCode;

    /** 仓库id */
    private Long warehouseId;

    /** 库存流水id */
    private Long stockFlowId;

    /** 作业类型 */
    private ReconciliationJobTypeEnum jobType;

    /** 差异类型 */
    private ReconciliationDiffTypeEnum diffType;

    private Date createTime;

    private Date updateTime;
}
