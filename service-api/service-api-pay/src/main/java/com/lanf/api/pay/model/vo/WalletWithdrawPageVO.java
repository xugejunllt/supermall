package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.WithdrawStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WalletWithdrawPageVO implements Serializable {

    private Long id;

    private Long userId;

    private Long walletAccountId;

    private String withdrawNo;

    private BigDecimal amount;

    private Integer withdrawType;

    private String payeeAccount;

    private WithdrawStatusEnum status;

    private String failReason;

    private String remark;

    private Long version;

    private Date createTime;

    private Date updateTime;
}
