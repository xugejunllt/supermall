package com.lanf.storage.model.entity;

import com.lanf.storage.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.storage.model.enums.ReconciliationJobTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReconciliationDiffDO implements Serializable {

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
