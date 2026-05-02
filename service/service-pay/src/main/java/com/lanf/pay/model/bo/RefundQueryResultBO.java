package com.lanf.pay.model.bo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RefundQueryResultBO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付宝交易号")
    private String tradeNo;

    @ApiModelProperty(value = "商户订单号")

    private String outTradeNo;
    @ApiModelProperty(value = "商户退款请求号")
    private String outRequestNo;


    /**
     * 本次退款请求，对应的退款金额。单位：元。
     */
    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;
    /**
     * 本次商户实际退回金额
     */
    @ApiModelProperty(value = "退款金额")
    private BigDecimal sendBackFee;

    @ApiModelProperty(value = "退款时间")
    private Date gmtRefundPay;



    /**
     * 退款结果
     */
    private Boolean result;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

}
