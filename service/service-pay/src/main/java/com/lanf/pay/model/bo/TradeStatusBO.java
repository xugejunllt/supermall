package com.lanf.pay.model.bo;

import com.lanf.pay.model.enums.TradeStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeStatusBO implements Serializable {


    @ApiModelProperty(value = "用户支付完成时间")
    private Date payFinishTime;

    @ApiModelProperty(value = "实收金额")

    private BigDecimal receiptMoney;

    /**
     * 订单总金额，单位为元，精确到小数点后两位
     */
    private BigDecimal totalAmount;

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
    /**
     * 交易状态
     */
    private TradeStatusEnum tradeStatus;
}
