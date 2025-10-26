package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LiquidationDTO implements Serializable {


    private Long orderId;
    //支付完成时间
    private Date payFinishTime;
    //订单来源 0:用户下单支付,1:履约完成,2:用户订单退款
    private Integer source;
    private BigDecimal payMoney;

    private String platformAccount;

    private Long shopId;

    private Integer accountType;

    //实收金额
    private BigDecimal receiptMoney;
}
