package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 去重标记
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */

@Data
@TableName("reconciliation_diff_marker")
public class ReconciliationDiffMarkerDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private String batchId;

    @ApiModelProperty(value = "业务单号（商户订单号）")
    private String businessOrderNo;

    @ApiModelProperty(value = "差异类型：0: 长款 1：短款")
    private ReconciliationDiffTypeEnum diffType;

    private ReconciliationBusinessTypeEnum businessType;



}
