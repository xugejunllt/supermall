package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 对账结果表
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@TableName("reconciliation_result")
@Data
public class ReconciliationResultDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "批次号，如 2026-04-29")
    private String batchId;

    @ApiModelProperty(value = "我方总笔数")
    private Integer totalMyCount;

    @ApiModelProperty(value = "我方总金额（元）")
    private BigDecimal totalMyAmount;

    @ApiModelProperty(value = "渠道总笔数")
    private Integer totalChannelCount;

    @ApiModelProperty(value = "渠道总金额（元）")
    private BigDecimal totalChannelAmount;

    @ApiModelProperty(value = "长款笔数（我方有，渠道无）")
    private Integer diffCountLong;

    @ApiModelProperty(value = "短款笔数（渠道有，我方无）")
    private Integer diffCountShort;

    @ApiModelProperty(value = "金额差异笔数（双方都有但金额不一致）")
    private Integer diffCountAmount;

    @ApiModelProperty(value = "对账状态: 0:处理中, 2: 已完成")
    private ReconciliationStatusEnum status;

    private Long version;




}
