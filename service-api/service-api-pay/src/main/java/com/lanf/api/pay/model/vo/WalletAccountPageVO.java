package com.lanf.api.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WalletAccountPageVO implements Serializable {

    private Long walletAccountId;

    private Long userId;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额（提现中金额）
     */
    private BigDecimal frozenBalance;

    private Date createTime;

}
