package com.lanf.pay.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class QueryRefundResultMessage extends BaseMessage {


    private String outTradeNo;

    private  String outRequestNo;


}
