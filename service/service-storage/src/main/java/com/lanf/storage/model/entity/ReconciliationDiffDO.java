package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.storage.model.enums.ReconciliationJobTypeEnum;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
@Data
@TableName("reconciliation_diff")
public class ReconciliationDiffDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** 批次id */
    private String bathId;

    private Long orderId;

    /** 商品sku编码 */
    private String skuCode;

    private Long warehouseId;

    private Long stockFlowId;

    private ReconciliationJobTypeEnum jobType;

    private ReconciliationDiffTypeEnum diffType;




}
