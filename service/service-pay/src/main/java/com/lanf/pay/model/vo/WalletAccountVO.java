package com.lanf.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WalletAccountVO implements Serializable {

    private Long walletAccountId;
    /**
     * 可用余额
     */
    private BigDecimal balance;

}
