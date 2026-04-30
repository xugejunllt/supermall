package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import io.swagger.annotations.ApiModelProperty;
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


    @ApiModelProperty(value = "批次号，如 2026-04-29")
    private String batchId;

    @ApiModelProperty(value = "业务单号（商户订单号）")
    private String businessOrderNo;

    @ApiModelProperty(value = "支付渠道")
    private PayChannelEnum payChannel;

    @ApiModelProperty(value = "预期金额（我方金额）")
    private BigDecimal expectedAmount;

    @ApiModelProperty(value = "实际金额（渠道金额）")
    private BigDecimal actualAmount;

    @ApiModelProperty(value = "差异金额（实际金额 - 预期金额）")
    private BigDecimal diffAmount;

    @ApiModelProperty(value = "0：长款,1：短款，2：金额不符")
    private ReconciliationDiffTypeEnum diffType;

    @ApiModelProperty(value = "交易发生时间")
    private Date occurTime;

    @ApiModelProperty(value = "0：支付，2: 退款  3: 转账 ")
    private ReconciliationBusinessTypeEnum businessType;




}
