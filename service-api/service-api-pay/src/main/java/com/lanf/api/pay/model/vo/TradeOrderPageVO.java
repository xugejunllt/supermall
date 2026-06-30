package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.PayMethodEnum;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeOrderPageVO implements Serializable {

    private Long id;

    private Long bathPayOrderId;

    private Long userId;

    private Long orderId;

    private String orderNumber;

    private String outTradeNo;

    private BigDecimal tradeMoney;

    private TradePurposeEnum tradePurpose;

    private PayMethodEnum payMethod;

    private PayChannelEnum payType;

    private Integer payStatus;

    private Integer bathPay;

    private Date expireTime;

    private Date createTime;

}
