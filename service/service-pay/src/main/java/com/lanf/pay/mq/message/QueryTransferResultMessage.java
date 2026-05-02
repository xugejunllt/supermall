package com.lanf.pay.mq.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryTransferResultMessage implements Serializable {

    @ApiModelProperty(value = "商家侧唯一订单号")
    private String outBizNo;



}
