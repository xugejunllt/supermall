package com.lanf.pay.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PrepayOrderDTO implements Serializable {


    //交易单号
    private String outTradeNo;
    //交易总金额
    private BigDecimal totalAmount;
    //是否是批量付款
    private  Boolean bathPay;
    //超时时间 单位分钟
    private Integer expireInterval;
}
