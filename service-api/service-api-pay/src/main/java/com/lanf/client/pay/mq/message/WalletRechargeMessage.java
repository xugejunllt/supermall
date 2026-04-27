package com.lanf.client.pay.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WalletRechargeMessage implements Serializable {

    private Long userId;
    /**
     * 流水号
     */
    private String flowNo;
    /**
     * 充值金额
     */
    private BigDecimal amount;

    private Long bizOrderId;
}
