package com.lanf.pay.mq.message;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.TransferFlowStatusEnum;
import com.lanf.pay.model.enums.TransferStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferQueryResultProcessorMessage implements Serializable {

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
    private TransferFlowStatusEnum status;

    @ApiModelProperty(value = "转账完成时间")
    private Date payFinishTime;

    @ApiModelProperty(value = "退款失败原因")
    private String failReason;


    private TransferStatusEnum updateTransferStatus;
}
