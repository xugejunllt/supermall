package com.lanf.pay.model.dto;

import com.lanf.pay.model.bo.PassbackParams;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PrepayOrderDTO implements Serializable {


    //交易单号
    private String outTradeNo;
    //交易总金额
    private BigDecimal totalAmount;

    //超时时间 单位分钟
    private Integer expireInterval;
    /**
     * 支付回调参数
     */
    private PassbackParams passbackParams;

}
