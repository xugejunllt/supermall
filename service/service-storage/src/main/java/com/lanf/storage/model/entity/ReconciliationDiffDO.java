package com.lanf.storage.model.entity;

import com.lanf.storage.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.storage.model.enums.ReconciliationJobTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value="ReconciliationDiff对象", description="")
public class ReconciliationDiffDO implements Serializable {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "批次id")
    private String bathId;

    private Long orderId;

    @ApiModelProperty(value = "商品sku编码")
    private String skuCode;

    private Long warehouseId;

    private Long stockFlowId;

    private ReconciliationJobTypeEnum jobType;

    private ReconciliationDiffTypeEnum diffType;




}
