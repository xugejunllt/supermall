package com.lanf.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SignCustomerFundBillDetailPageVO implements Serializable {

    private Long id;

    private PayChannelEnum payChannel;

    private String payFinishDate;

    private String merchantOrderNo;

    private String financialSerialNo;

    private String businessSerialNo;

    private LocalDateTime occurTime;

    private String counterpartyAccount;

    private BigDecimal incomeAmount;

    private BigDecimal expenseAmount;

    private BigDecimal accountBalance;

    private String transactionChannel;

    private ReconciliationBusinessTypeEnum businessType;

    private String remark;

    private Date createTime;
}
