package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.api.pay.model.enums.TransferStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferOrderPageVO implements Serializable {

    private Long id;

    private String outTradeNo;

    private Long userId;

    private Long merchantId;

    private Long bizOrderId;

    private String fromAccount;

    private String incomeAccount;

    private String incomeAccountUserName;

    private TransferEventTypeEnum eventType;

    private PayChannelEnum transferChannel;

    private BigDecimal totalAmount;

    private TransferStatusEnum status;

    private Date createTime;

}
