package com.lanf.pay.mq.message;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import com.lanf.api.pay.model.enums.RefundStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RefundQueryResultProcessorMessage implements Serializable {


    @ApiModelProperty(value = "商户订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "退款请求号， 标识一次退款请求")
    private String outRequestNo;

    @ApiModelProperty(value = "支付宝交易号")
    private String tradeNo;

    @ApiModelProperty(value = "实际支付的金额")
    private BigDecimal payMoney;

    @ApiModelProperty(value = "实际退款金额")
    private BigDecimal returnMoney;



    /**
     * 0:退款成功 1：退款失败
     */
    private RefundFlowStatusEnum status;

    @ApiModelProperty(value = "支付订单ID")
    private Long payOrderId;

    private PayChannelEnum payChannelEnum;

    @ApiModelProperty(value = "退款完成时间")
    private Date payFinishTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 更新的退款单状态
     */
    private RefundStatusEnum updateStatusRefundStatus;

}
