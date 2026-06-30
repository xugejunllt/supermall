package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferOrderFlowPageVO implements Serializable {

    private Long id;

    private String outTradeNo;

    private PayChannelEnum transferChannel;

    private String fromAccount;

    private String incomeAccount;

    private BigDecimal totalAmount;

    private Date payFinishTime;

    private Date createTime;

}
