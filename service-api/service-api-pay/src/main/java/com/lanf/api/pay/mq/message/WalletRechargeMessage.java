package com.lanf.api.pay.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRechargeMessage
        extends BaseMessage {

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
