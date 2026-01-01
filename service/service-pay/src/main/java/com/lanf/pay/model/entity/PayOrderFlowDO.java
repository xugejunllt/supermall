package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 支付流水
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
@Data
@TableName("pay_order_flow")
public class PayOrderFlowDO implements Serializable {

private static final long serialVersionUID=1L;



    private String outTradeNo;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeMoney;

    @ApiModelProperty(value = "实收金额")
    private BigDecimal receiptMoney;

    @ApiModelProperty(value = "用户支付完成时间")
    private Date payFinishTime;

    @ApiModelProperty(value = "支付账户")
    private String payAccount;

    @ApiModelProperty(value = "收款账户")
    private String incomeAccount;

    @ApiModelProperty(value = "通知时间")
    private Date notifyTime;

    @ApiModelProperty(value = "支付宝交易号。支付宝交易凭证号。")
    private String tradeNo;




}
