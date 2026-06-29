package com.lanf.pay.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class QueryTransferResultMessage extends BaseMessage  {

    @ApiModelProperty(value = "商家侧唯一订单号")
    private String outBizNo;

    private Date transDate;

}
