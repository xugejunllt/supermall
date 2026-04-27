package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
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
@TableName("wallet_account")
public class WalletAccountDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private Long userId;

    @ApiModelProperty(value = "可用余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "冻结余额（提现中金额）")
    private BigDecimal frozenBalance;

    private Long version;




}
