package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationTradeStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 对账差异明细表
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Data
@TableName("reconciliation_diff")
public class ReconciliationDiffDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /**
     * 批次号，如 2026-04-29
     */
    private String batchId;

    /**
     * 业务单号（商户订单号）
     */
    private String businessOrderNo;

    /**
     * 支付渠道
     */
    private PayChannelEnum payChannel;

    /**
     * 预期金额（我方金额）
     */
    private BigDecimal expectedAmount;

    /**
     * 实际金额（渠道金额）
     */
    private BigDecimal actualAmount;

    /**
     * 我方状态 0:交易成功 1：交易失败
     */
    private ReconciliationTradeStatusEnum expectedStatus;
    /**
     * 三方状态 0:交易成功 1：交易失败
     */
    private ReconciliationTradeStatusEnum actualStatus;

    /**
     * 差异金额（实际金额 - 预期金额）
     */
    private BigDecimal diffAmount;

    /**
     * 0：长款,1：短款，2：金额不符
     */
    private ReconciliationDiffTypeEnum diffType;

    /**
     * 交易发生时间
     */
    private Date occurTime;

    /**
     * 0：支付，2: 退款  3: 转账 
     */
    private ReconciliationBusinessTypeEnum businessType;




}
