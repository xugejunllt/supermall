package com.lanf.api.pay.mq.message;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.constant.mq.base.BaseMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferMessage extends BaseMessage {


    @ApiModelProperty(value = "商家侧唯一订单号")
    private String outBizNo;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "商家id")
    private Long merchantId;

    @ApiModelProperty(value = "关联事件对应的业务单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "事件类型 0：订单结算给商家，1：用户钱包提现")
    private TransferEventTypeEnum eventType;

    @ApiModelProperty(value = "转账渠道：1-支付宝，2-微信支付，3-银行卡")
    private PayChannelEnum transferChannel;

    @ApiModelProperty(value = "转账来源账户")
    private String fromAccount;

    @ApiModelProperty(value = "收款账号")
    private String incomeAccount;

    private String incomeAccountUserName;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "转账业务的标题，用于在支付宝用户的账单里显示")
    private String orderTitle;

}
