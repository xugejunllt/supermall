package com.lanf.api.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayOrderFlowPageVO implements Serializable {

    private Long id;

    private Long tradeId;

    private Integer payType;

    private String outTradeNo;

    private BigDecimal tradeMoney;

    private BigDecimal receiptMoney;

    private Date payFinishTime;

    private String payAccount;

    private String incomeAccount;

    private Date notifyTime;

    private String tradeNo;

    private Date createTime;

}
