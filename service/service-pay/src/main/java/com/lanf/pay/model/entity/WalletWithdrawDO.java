package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 钱包提现记录表
 * </p>
 *
 * @author jarven
 * @since 2026-04-28
 */
@Data
@TableName("wallet_withdraw")
public class WalletWithdrawDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "钱包账户ID")
    private Long walletAccountId;

    @ApiModelProperty(value = "提现单号")
    private String withdrawNo;

    @ApiModelProperty(value = "提现金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "提现方式：1-支付宝，2-银行卡")
    private Integer withdrawType;

    @ApiModelProperty(value = "收款账户")
    private String payeeAccount;

    @ApiModelProperty(value = "提现状态：0-待处理，1-处理中，2-成功，3-失败，4-已取消")
    private Integer status;


    @ApiModelProperty(value = "失败原因")
    private String failReason;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "版本号（乐观锁）")
    private Long version;



}
