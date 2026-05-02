package com.lanf.pay.model.entity;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 转账单
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TransferOrderFlow对象", description="转账单")
public class TransferOrderFlow implements Serializable {

private static final long serialVersionUID=1L;


    private String outTradeNo;

    @ApiModelProperty(value = "转账渠道：1-支付宝，2-微信支付，3-银行卡")
    private PayChannelEnum transferChannel;

    @ApiModelProperty(value = "转账来源账户")
    private String fromAccount;

    @ApiModelProperty(value = "收款账号")
    private String incomeAccount;

    @ApiModelProperty(value = "订单总金额，即发起转账时传入的金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "实际转账金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "0:退款成功 1：退款失败")
    private Integer status;

    @ApiModelProperty(value = "转账完成时间")
    private Date payFinishTime;

    private String payFinishDate;

    @ApiModelProperty(value = "退款失败原因")
    private String failReason;




}
