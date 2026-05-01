package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 三方支付资金账单明细表
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
@Data
@TableName("fund_bill_detail")
public class FundBillDetailDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private PayChannelEnum payChannel;

    private String payFinishDate;

    @ApiModelProperty(value = "商户订单号（原始订单号）")
    private String merchantOrderNo;

    @ApiModelProperty(value = "财务流水号（支付宝内部唯一凭证号）")
    private String financialSerialNo;

    @ApiModelProperty(value = "业务流水号（商户订单号或退款请求号）")
    private String businessSerialNo;

    @ApiModelProperty(value = "发生时间（资金变动精确时间）")
    private LocalDateTime occurTime;

    @ApiModelProperty(value = "对方账号（脱敏账号）")
    private String counterpartyAccount;

    @ApiModelProperty(value = "收入金额（+元），支出时此字段为0")
    private BigDecimal incomeAmount;

    @ApiModelProperty(value = "支出金额（-元），收入时此字段为0")
    private BigDecimal expenseAmount;

    @ApiModelProperty(value = "账户余额（资金变动完成后实时余额）")
    private BigDecimal accountBalance;

    @ApiModelProperty(value = "交易渠道（如：支付宝、天猫等）")
    private String transactionChannel;

    @ApiModelProperty(value = "业务类型（如：在线支付、退款、转账、商家扣款）")
    private ReconciliationBusinessTypeEnum businessType;

    @ApiModelProperty(value = "备注")
    private String remark;




}
