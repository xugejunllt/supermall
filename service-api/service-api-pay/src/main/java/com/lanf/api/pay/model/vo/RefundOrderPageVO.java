package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.RefundStatusEnum;
import com.lanf.constant.model.enums.pay.RefundEventTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RefundOrderPageVO implements Serializable {

    private Long id;

    private String outTradeNo;

    private BigDecimal returnMoney;

    private RefundStatusEnum status;

    private RefundEventTypeEnum refundEventType;

    private PayChannelEnum payChannel;

    private Long bizOrderId;

    private String refundReason;

    private Date createTime;

}
