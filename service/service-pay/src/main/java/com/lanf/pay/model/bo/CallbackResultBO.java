package com.lanf.pay.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CallbackResultBO implements Serializable {



    @ApiModelProperty(value = "用户支付完成时间")
    private Date payFinishTime;

    @ApiModelProperty(value = "实收金额")

    private BigDecimal receiptMoney;

    private String payAccount;

    @ApiModelProperty(value = "收款账户")
    private String incomeAccount;

    @ApiModelProperty(value = "通知时间")
    private Date notifyTime;
    /**
     * 支付宝交易号。支付宝交易凭证号。
     *
     *
     */
    private String tradeNo;
    /**
     * 商户订单号
     */
    private String outTradeNo;
    //是否是批量支付
    private  Boolean bathPay;
}
