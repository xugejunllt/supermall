package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RefundOrderFlowPageVO implements Serializable {

    private Long id;

    private Long refundOrderId;

    private String outTradeNo;

    private String outRequestNo;

    private String tradeNo;

    private BigDecimal payMoney;

    private BigDecimal returnMoney;


    private PayChannelEnum payChannel;

    private Date payFinishTime;

    private Date createTime;

}
