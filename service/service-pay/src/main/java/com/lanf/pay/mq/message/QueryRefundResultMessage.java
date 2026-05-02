package com.lanf.pay.mq.message;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryRefundResultMessage implements Serializable {


    private String outTradeNo;

    private  String outRequestNo;

    private PayChannelEnum payChannel;
    @ApiModelProperty(value = "支付订单ID")
    private Long payOrderId;
}
