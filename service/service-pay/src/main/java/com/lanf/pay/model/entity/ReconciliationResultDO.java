package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationStatusEnum;
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


    /**
     * 批次号，如 2026-04-29
     */
    private String batchId;

    /**
     * 我方总笔数
     */
    private Integer totalMyCount;

    /**
     * 我方总金额（元）
     */
    private BigDecimal totalMyAmount;

    /**
     * 渠道总笔数
     */
    private Integer totalChannelCount;

    /**
     * 渠道总金额（元）
     */
    private BigDecimal totalChannelAmount;

    /**
     * 长款笔数（我方有，渠道无）
     */
    private Integer diffCountLong;

    /**
     * 短款笔数（渠道有，我方无）
     */
    private Integer diffCountShort;

    /**
     * 金额差异笔数（双方都有但金额不一致）
     */
    private Integer diffCountAmount;

    /**
     * 对账状态: 0:处理中, 2: 已完成
     */
    private ReconciliationStatusEnum status;

    private Long version;




}
