package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.WalletEventTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 钱包账户表
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
@Data
@TableName("wallet_account_flow")
public class WalletAccountFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "流水号")
    private String flowNo;

    private Long walletAccountId;

    private Long userId;

    @ApiModelProperty(value = "变更前余额")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "变更后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "变更余额")
    private BigDecimal changeBalance;

    @ApiModelProperty(value = "关联的业务单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "0:充值，1：提现，2：下单")
    private WalletEventTypeEnum eventType;



}
